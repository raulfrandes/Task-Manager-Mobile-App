import React, { useCallback, useContext, useEffect, useReducer, useState } from "react";
import { getLogger } from "../core";
import { TaskProps } from "../core/TaskProps";
import PropTypes, { func } from 'prop-types';
import { createTask, deleteTask as deleteTaskApi, getTasks, newWebSocket, updateTask } from "../core/taskApi";
import { AuthContext } from "../auth/AuthProvider";
import { useNetwork } from "../pages/useNetwork";
import { Preferences } from "@capacitor/preferences";

const log = getLogger('TaskProvider');

type SaveTaskFn = (task: TaskProps) => Promise<any>;
type DeleteTaskFn = (taskId: number) => Promise<any>;
type SetSelectedTaskFn = (task: TaskProps | null) => void;
type FetchMoreTasksFn = ($event: CustomEvent<void>) => void;

export interface TasksState {
    tasks: TaskProps[],
    fetching: boolean,
    fetchingError?: Error | null,
    saving: boolean,
    savingError?: Error | null,
    saveTask?: SaveTaskFn,
    deleting: boolean,
    deletingError?: Error | null,
    deleteTask?: DeleteTaskFn,
    selectedTask: TaskProps | null,
    setSelectedTask?: SetSelectedTaskFn,
    fetchMoreTasks?: FetchMoreTasksFn,
    hasMore: boolean,
    totalTasks: number,
    setSearchQuery?: (query: string) => void,
    setFilterCompleted?: (filter: "all" | "completed" | "incompleted") => void,
    unsyncedCount: number
}

interface ActionProps {
    type: string,
    payload?: any
}

const initialState: TasksState = {
    tasks: [],
    fetching: false,
    saving: false,
    deleting: false,
    selectedTask: null,
    hasMore: true,
    totalTasks: 0,
    unsyncedCount: 0
};

const FETCH_TASKS_STARTED = 'FETCH_TASKS_STARTED';
const FETCH_TASKS_SUCCEEDED = 'FETCH_TASKS_SUCCEEDED';
const FETCH_TASKS_FAILED = 'FETCH_TASKS_FAILED';
const FETCH_TASKS_APPEND = 'FETCH_TASKS_APPEND';
const SAVE_TASK_STARTED = 'SAVE_TASK_STARTED';
const SAVE_TASK_SUCCEEDED = 'SAVE_TASK_SUCCEEDED';
const SAVE_TASK_FAILED = 'SAVE_TASK_FAILED';
const DELETE_TASK_STARTED = 'DELETE_TASK_STARTED';
const DELETE_TASK_SUCCEEDED = 'DELETE_TASK_SUCCEEDED';
const DELETE_TASK_FAILED = 'DELETE_TASK_FAILED';
const SELECT_TASK = 'SELECT_TASK';
const DESELECT_TASK = 'DESELECT_TASK';
const UNSYNCED_TASKS_UPDATED = 'UNSYNCED_TASKS_UPDATED'

const UNSYNCED_TASKS_KEY = 'unsyncedTasks';

const reducer: (state: TasksState, action: ActionProps) => TasksState =
    (state, { type, payload }) => {
        switch(type) {
            case FETCH_TASKS_STARTED:
                return { ...state, fetching: true, fetchingError: null };
            case FETCH_TASKS_SUCCEEDED:
                console.log("SUCCEEDED:", payload.tasks.length + (state.tasks?.length || 0) < payload.totalTasks, payload.tasks)
                return { 
                    ...state, 
                    tasks: payload.tasks, 
                    fetching: false,
                    totalTasks: payload.totalTasks,
                    hasMore: payload.tasks.length + (state.tasks?.length || 0) < payload.totalTasks
                 };
            case FETCH_TASKS_FAILED:
                return { ...state, fetchingError: payload.error, fetching: false };
            case FETCH_TASKS_APPEND:
                console.log("APPEND:", payload.tasks.length + (state.tasks?.length || 0) < state.totalTasks)
                return { 
                    ...state, 
                    tasks: [...(state.tasks || []), ...payload.tasks],
                    fetching: false,
                    hasMore: payload.tasks.length + (state.tasks?.length || 0) < state.totalTasks
                };
            case SAVE_TASK_STARTED:
                return { ...state, savingError: null, saving: true };
            case SAVE_TASK_SUCCEEDED:
                const tasks = [...(state.tasks || [])];
                const task = payload.task;
                const index = tasks.findIndex(t => t.id === task.id);
                if (index === -1) {
                    tasks.push(task);
                } else {
                    tasks[index] = task;
                }
                return { ...state, tasks: tasks, saving: false };
            case SAVE_TASK_FAILED:
                return { ...state, savingError: payload.error, saving: false };
            case DELETE_TASK_STARTED:
                return { ...state, deletingError: null, deleting: true };
            case DELETE_TASK_SUCCEEDED:
                const prevTasks = [...(state.tasks || [])];
                const taskId = payload.taskId
                const currTasks = prevTasks.filter(t => t.id !== taskId);
                return { ...state, tasks: currTasks, deleting: false };
            case DELETE_TASK_FAILED:
                return { ...state, deletingError: payload.error, deleting: false };
            case SELECT_TASK:
                return { ...state, selectedTask: payload.task };
            case DESELECT_TASK:
                return { ...state, selectedTask: null };
            case UNSYNCED_TASKS_UPDATED:
                return { ...state, unsyncedCount: payload.count };
            default:
                return state;
        }
    };

export const TaskContext = React.createContext<TasksState>(initialState);

interface TaskProviderProps {
    children: PropTypes.ReactNodeLike
}

export const TaskProvider: React.FC<TaskProviderProps> = ({children}) => {
    const { networkStatus } = useNetwork();
    const isOnline = networkStatus.connected;
    const { token } = useContext(AuthContext);
    const [state, dispatch] = useReducer(reducer, initialState);
    const { tasks, fetching, fetchingError, saving, savingError, deleting, deletingError, selectedTask, hasMore, totalTasks, unsyncedCount } = state;
    
    const [skip, setSkip] = useState(0);
    const take = 10;
    const [searchQuery, setSearchQueryState] = useState<string>("");
    const [completedFilter, setCompletedFilter] = useState<"all" | "completed" | "incompleted">("all"); 

    useEffect(getTasksEffect, [token, searchQuery, completedFilter]);
    useEffect(wsEffect, [token]);
    useEffect(() => {
        if (isOnline) {
            syncOfflineTasks();
        }
    }, [isOnline]);

    const fetchMoreTasks = useCallback<FetchMoreTasksFn>(fetchMoreTasksCallback, [token, hasMore, skip]);
    const saveTask = useCallback<SaveTaskFn>(saveTaskCallback, [token]);
    const deleteTask = useCallback<DeleteTaskFn>(deleteTaskCallback, [token]);
    const setSelectedTask = useCallback<SetSelectedTaskFn>(setSelectedTaskCallback, [token]);
    
    const setSearchQuery = (query: string) => {
        setSearchQueryState(query);
        setSkip(0);
        dispatch({ type: FETCH_TASKS_SUCCEEDED, payload: {tasks: [], totalTasks: 0} })
    }

    const setFilterCompleted = (filter: "all" | "completed" | "incompleted") => {
        setCompletedFilter(filter);
        setSkip(0);
        dispatch({ type: FETCH_TASKS_SUCCEEDED, payload: {tasks: [], totalTasks: 0} })
    }

    const value = { 
        tasks, 
        fetching, 
        fetchingError, 
        saving, 
        savingError, 
        saveTask, 
        deleting, 
        deletingError, 
        deleteTask, 
        selectedTask, 
        setSelectedTask, 
        fetchMoreTasks,
        hasMore,
        totalTasks,
        setSearchQuery,
        setFilterCompleted,
        unsyncedCount
    };

    return (
        <TaskContext.Provider value={value}>
            {children}
        </TaskContext.Provider>
    );

    async function fetchMoreTasksCallback($event: CustomEvent<void>) {
        if (hasMore) await fetchTasks(false);
        await ($event.target as HTMLIonInfiniteScrollElement).complete();
    }

    function getTasksEffect() {
        let canceled = false;
        if (token) {
            fetchTasks(true);
        }
        return () => {
            canceled = true;
        }
    }

    async function fetchTasks(isNewQuery = false) {
        if (!token) return;
        try {
            log('fetchTasks started');
            dispatch({ type: FETCH_TASKS_STARTED });
            const result = await getTasks(token, { 
                searchQuery, 
                skip: isNewQuery ? 0 : skip, 
                take, 
                completedFilter: completedFilter !== "all" ? completedFilter === "completed" : undefined });
            dispatch({ 
                type: isNewQuery ? FETCH_TASKS_SUCCEEDED : FETCH_TASKS_APPEND, 
                payload: { 
                    tasks: result.tasks,
                    totalTasks: result.totalTasks
                }
            });
            if (isNewQuery) setSkip(take);
            else setSkip(prev => prev + take);
            log('fetchTasks succeeded');
        } catch (error) {
            log('fetchTasks failed');
            dispatch({ type: FETCH_TASKS_FAILED, payload: { error } });
        }
    }

    function wsEffect() {
        let canceled = false;
        log('wsEffect - connecting');
        const closeWebSocket = newWebSocket(token, message => {
            if (canceled) {
                return;
            }
            const { eventType, payload: { task } } = message;
            log(`ws message, task ${eventType}`);
            if (eventType === 'TaskAdded' || eventType === 'TaskUpdated') {
                dispatch({ type: SAVE_TASK_SUCCEEDED, payload: { task } });
            }
            if (eventType === 'TaskUpdated') {
                dispatch({ type: DESELECT_TASK });
            }
            if (eventType === 'TaskDeleted') {
                console.log('in wsEffect - ', task);
                const taskId = task?.id;
                dispatch({ type: DELETE_TASK_SUCCEEDED, payload: { taskId } });
                dispatch({ type: DESELECT_TASK });
            }
        });
        return () => {
            log('wsEffect - disconnecting');
            canceled = true;
            closeWebSocket();
        }
    }

    async function saveTaskCallback(task: TaskProps) {
        if (isOnline) {
            try {
                log('saveTask started');
                dispatch({ type: SAVE_TASK_STARTED });
                const savedTask = await (task.id ? updateTask(token, task) : createTask(token, task));
                log('saveTask succeeded');
                dispatch({ type: SAVE_TASK_SUCCEEDED, payload: {task: savedTask} });
            } catch (error) {
                log('saveTask failed');
                await handleUnsyncedTask('save', task);
                dispatch({ type: SAVE_TASK_FAILED, payload: { error } });
            }
        } else {
            await handleUnsyncedTask('save', task);
            dispatch({ type: SAVE_TASK_SUCCEEDED, payload: { task } });
        }
    }

    async function deleteTaskCallback(taskId: number) {
        if (isOnline) {
            try {
                log('deleteTask started');
                dispatch({ type: DELETE_TASK_STARTED });
                await deleteTaskApi(token ,taskId);
                log('deleteTask succeeded');
                dispatch({ type: DELETE_TASK_SUCCEEDED, payload: {taskId} });
            } catch (error) {
                log('deleteTask failed');
                await handleUnsyncedTask('delete', { id: taskId });
                dispatch({ type: DELETE_TASK_FAILED, payload: { error } });
            }
        } else {
            await handleUnsyncedTask('delete', { id: taskId });
            dispatch({ type: DELETE_TASK_SUCCEEDED, payload: {taskId} });
        }
    }

    function setSelectedTaskCallback(task: TaskProps | null) {
        task ? dispatch({ type: SELECT_TASK, payload: { task } })
            : dispatch({ type: DESELECT_TASK });
    }

    async function handleUnsyncedTask(action: 'save' | 'delete', task: Partial<TaskProps>) {
        const { value } = await Preferences.get({ key: UNSYNCED_TASKS_KEY });
        const unsyncedTasks = value ? JSON.parse(value) : [];
        unsyncedTasks.push({ action, task });
        await Preferences.set({ key: UNSYNCED_TASKS_KEY, value: JSON.stringify(unsyncedTasks) });
        dispatch({ type: UNSYNCED_TASKS_UPDATED, payload: { count: unsyncedTasks.length } })
    }

    async function syncOfflineTasks() {
        const { value } = await Preferences.get({ key: UNSYNCED_TASKS_KEY });
        const unsyncedTasks = value ? JSON.parse(value) : [];

        for (const { action, task } of unsyncedTasks) {
            try {
                if (action === 'save') {
                    await (task.id? updateTask(token, task as TaskProps) : createTask(token, task as TaskProps));
                } else if (action === 'delete') {
                    await deleteTaskApi(token, task.id as number);
                } 
                await removeSyncedTask({ action, task });
            } catch (error) {
                console.error('Failed to sync task: ', error);
            }
        }
    }

    async function removeSyncedTask(taskToRemove: { action: string; task: TaskProps }) {
        const { value } = await Preferences.get({ key: UNSYNCED_TASKS_KEY });
        const unsyncedTasks = value ? JSON.parse(value) : [];
        const updatedTasks = unsyncedTasks.filter(
            (task: { action: string; task: { id: number | undefined; }; }) => task.action !== taskToRemove.action && task.task.id !== taskToRemove.task.id
        );
        await Preferences.set({ key: UNSYNCED_TASKS_KEY, value: JSON.stringify(updatedTasks) });
        dispatch({ type: UNSYNCED_TASKS_UPDATED, payload: { count: updatedTasks.length } })
    }
}