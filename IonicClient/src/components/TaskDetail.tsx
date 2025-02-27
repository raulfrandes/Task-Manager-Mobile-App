import { useContext, useState } from "react";
import { TaskProps } from "../core/TaskProps";
import { TaskContext } from "./TaskProvider";
import { IonButton, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonImg, IonLoading } from "@ionic/react";
import TaskModal from "./TaskModal";
import React from "react";
import MyMap from "../map/MyMap";

type Props = {
    task: TaskProps;
    onBack: () => void;
}

const priorityMapping: {[key : number] : string} = {
    1: 'Low',
    2: 'Medium',
    3: 'High'
}

const TaskDetail: React.FC<Props> = ({ task, onBack }) => {
    const [showTaskModal, setShowTaskModal] = useState<boolean>(false);
    const [showMap, setShowMap] = useState<boolean>(false);
    const { deleting, deletingError, deleteTask } = useContext(TaskContext);

    const handleDeleteTask = async () => {
        const confirmDelete = window.confirm('Are you sure you want to delete this task?');
        if (confirmDelete) {
            try {
                await deleteTask?.(task.id!);
                onBack();
            } catch (err) {
                console.error(err);
                alert('Failed to delete task.');
            }
        }
    };

    return (
        <>
            <IonLoading isOpen={deleting} message={"Deleting task"} />

            <IonCard>
                <IonCardHeader>
                    <IonCardTitle>Task Details</IonCardTitle>
                </IonCardHeader>
                <IonCardContent>
                    {task.photoUrl && 
                        <IonImg src={task.photoUrl} style={{ width: '100%', height: 'auto', marginTop: '1em'}} />
                    }
                    <p><strong>Title:</strong> {task.title}</p>
                    <p><strong>Description:</strong> {task.description}</p>
                    <p><strong>Due Date:</strong> {new Date(task.dueDate).toLocaleDateString()}</p>
                    <p><strong>Prioriry:</strong> { priorityMapping[task.priority] || 'Unknown' }</p>
                    <p><strong>Completed:</strong> {task.completed ? 'Yes' : 'No'}</p>
                    <IonButton onClick={() => setShowMap(true)} color="primary">View Location</IonButton>
                    {showMap && task.latitude && task.longitude && (
                        <MyMap
                            lat={task.latitude}
                            lng={task.longitude}
                            onMapClick={() => {}}
                            onMarkerClick={() => {}}
                        />
                    )}

                    <IonButton onClick={() => setShowTaskModal(true)} color="primary">Edit Task</IonButton>
                    <IonButton onClick={handleDeleteTask} color="danger" style={{marginLeft: '10px'}}>
                        Delete Task
                    </IonButton>
                    <IonButton onClick={onBack} style={{marginLeft: '10px'}}>
                        Back to Task List
                    </IonButton>
                </IonCardContent>
            </IonCard>

            {deletingError && ( 
                <div>{deletingError.message || 'Failed to fetch tasks'}</div>
            )}

            <TaskModal show={showTaskModal} task={task} onClose={() => setShowTaskModal(false)}></TaskModal>
        </>
    );
};

export default React.memo(TaskDetail);