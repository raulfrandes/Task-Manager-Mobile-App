import axios from "axios";
import { authConfig, baseUrl, getLogger, withLogs } from ".";
import { TaskProps } from "./TaskProps";

const taskUrl = `https://${baseUrl}/api/tasks`;

export const getTasks = (
    token: string,
    params: { searchQuery?: string; skip: number; take: number; completedFilter?: boolean }
): Promise<{ tasks: TaskProps[]; totalTasks: number}> => {
    const { searchQuery, skip, take, completedFilter } = params;

    const queryParams = new URLSearchParams({
        skip: skip.toString(),
        take: take.toString(),
    });

    if (searchQuery) {
        queryParams.append("searchQuery", searchQuery);
    }

    if (completedFilter !== undefined) {
        queryParams.append("completed", completedFilter.toString());
    }

    const url = `${taskUrl}?${queryParams.toString()}`;

    return withLogs(axios.get(url, authConfig(token)), 'getTasks');
}

export const createTask: (token: string, task: TaskProps) => Promise<TaskProps> = (token, task) => {
    return withLogs(axios.post(taskUrl, task, authConfig(token)), 'createTask');
}

export const updateTask: (token: string, task: TaskProps) => Promise<TaskProps> = (token, task) => {
    console.log(task.latitude)
    console.log(task.longitude)
    return withLogs(axios.put(`${taskUrl}/${task.id}`, task, authConfig(token)), 'updateTask');
}

export const deleteTask: (token: string, taskId: number) => Promise<void> = (token, taskId) => {
    return withLogs(axios.delete(`${taskUrl}/${taskId}`, authConfig(token)), 'deleteTask');
}

interface MessageData {
    eventType: string;
    payload: {
        task: TaskProps;
    };
}

const log = getLogger('ws');

export const newWebSocket = (token: string, onMessage: (data: MessageData) => void) => {
    const ws = new WebSocket(`wss://${baseUrl}/ws`)
    ws.onopen = () => {
        log('web socket onopen');
        ws.send(JSON.stringify({ token }));
    };
    ws.onclose = () => {
        log('web socket onclose');
    };
    ws.onerror = error => {
        log('web socket onerror', error);
    };
    ws.onmessage = messageEvent => {
        log('web socket onmessage');
        onMessage(JSON.parse(messageEvent.data));
    };
    return () => {
        ws.close();
    }
}