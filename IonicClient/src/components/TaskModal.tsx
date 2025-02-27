import { useContext, useEffect, useState } from "react";
import { TaskProps } from "../core/TaskProps";
import { TaskContext } from "./TaskProvider";
import { IonButton, IonContent, IonFabButton, IonHeader, IonIcon, IonImg, IonInput, IonItem, IonLabel, IonLoading, IonModal, IonSelect, IonSelectOption, IonTextarea, IonTitle, IonToolbar } from "@ionic/react";
import React from "react";
import { camera } from "ionicons/icons";
import { usePhotos } from "../camera/usePhotos";
import MyMap from "../map/MyMap";
import { enterAnimation, leaveAnimation } from "../animation/modalAnimations";

type Props = {
    show: boolean;
    task: TaskProps | null;
    onClose: () => void;
}

const defaultLat = 46.77;
const defaultLng = 23.6;

const TaskModal: React.FC<Props> = ({ show, task, onClose }) => {
    const { saving, savingError, saveTask, selectedTask, setSelectedTask } = useContext(TaskContext);
    const { takePhoto } = usePhotos();
    const [title, setTitle] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [dueDate, setDueDate] = useState<string>('');
    const [priority, setPriority] = useState<number>(1);
    const [completed, setCompleted] = useState<boolean>(false);
    const [photoUrl, setPhotoUrl] = useState<string | undefined>(undefined);
    const [showMap, setShowMap] = useState<boolean>(false);
    const [latitude, setLatitude] = useState<number | undefined>(task?.latitude);
    const [longitude, setLongitude] = useState<number | undefined>(task?.longitude);

    useEffect(() => {
        if (task) {
            setTitle(task.title);
            setDescription(task.description);
            const date = new Date(task.dueDate);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const formattedDueDate = `${year}-${month}-${day}`;
            setDueDate(formattedDueDate);
            setPriority(task.priority);
            setCompleted(task.completed);
            setPhotoUrl(task.photoUrl);
            setLatitude(task.latitude)
            setLongitude(task.longitude);
        } else {
            setTitle('');
            setDescription('');
            setDueDate('');
            setPriority(1);
            setCompleted(false);
            setPhotoUrl(undefined);
            setLatitude(undefined)
            setLongitude(undefined);
        }
    }, [show, task]);

    const handleSave = async () => {
        if (!title || !dueDate) {
            alert('Title and Due Date are required!');
            return;
        }

        const taskData: TaskProps = { 
            id: task?.id, 
            title, 
            description, 
            dueDate, 
            priority, 
            completed,
            photoUrl,
            latitude,
            longitude
        };

        try {
            await saveTask?.(taskData);
            onClose();
            if (selectedTask) {
                setSelectedTask?.(taskData);
            }
        } catch (error) {
            console.error('Failed to save task:', error);
            alert('Failed to save task.');
        }
    };

    const handleTakePhoto = async () => {
        const newPhoto = await takePhoto(task?.id!);
        setPhotoUrl(newPhoto.webviewPath);
    }

    const handleLocationSelect = (location: { latitude: number, longitude: number }) => {
        setLatitude(location.latitude);
        setLongitude(location.longitude);
        setShowMap(false);
    }

    return (
        <IonModal 
            isOpen={show}
            enterAnimation={enterAnimation}
            leaveAnimation={leaveAnimation} 
            onDidDismiss={onClose}
        >
            <IonHeader>
                <IonToolbar>
                    <IonTitle>{task ? 'Edit Task' : 'Add Task'}</IonTitle>
                    <IonButton slot="end" onClick={onClose}>Close</IonButton>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                <IonLoading isOpen={saving} message={"Saving task"} />

                <IonItem>
                    <IonLabel position='stacked'>Title</IonLabel>
                    <IonInput value={title} onIonChange={e => setTitle(e.detail.value!)} required />
                </IonItem>
                <IonItem>
                    <IonLabel position='stacked'>Description</IonLabel>
                    <IonTextarea value={description} onIonChange={e => setDescription(e.detail.value!)} />
                </IonItem>
                <IonItem>
                    <IonLabel position='stacked'>Due Date</IonLabel>
                    <IonInput type="date" value={dueDate} onIonChange={e => setDueDate(e.detail.value!)} required/>
                </IonItem>
                <IonItem>
                    <IonLabel position="stacked">Priority</IonLabel>
                    <IonSelect value={priority} onIonChange={e => setPriority(parseInt(e.detail.value!))}>
                        <IonSelectOption value={1}>Low</IonSelectOption>
                        <IonSelectOption value={2}>Medium</IonSelectOption>
                        <IonSelectOption value={3}>High</IonSelectOption>
                    </IonSelect>
                </IonItem>
                <IonItem>
                    <IonLabel>Completed</IonLabel>
                    <IonSelect value={completed} onIonChange={e => setCompleted(e.detail.value === true)}>
                        <IonSelectOption value={true}>Yes</IonSelectOption>
                        <IonSelectOption value={false}>No</IonSelectOption>
                    </IonSelect>
                </IonItem>

                <IonItem>
                    <IonLabel>Location</IonLabel>
                    <IonButton onClick={() => setShowMap(true)}>Select Location</IonButton>
                </IonItem>

                {showMap && (
                    <MyMap
                        lat={latitude || defaultLat}
                        lng={longitude || defaultLng}
                        onMapClick={handleLocationSelect}
                        onMarkerClick={(e) => console.log('Marker clicked', e)}
                    />
                )}

                <IonItem>
                    <IonLabel>Photo</IonLabel>
                    <IonFabButton onClick={handleTakePhoto}>
                        <IonIcon icon={camera} />
                    </IonFabButton>
                </IonItem>
                {photoUrl && <IonImg src={photoUrl} />}

                {savingError && ( 
                    <div>{savingError.message || 'Failed to save task'}</div>
                )}

                <IonButton expand='block' onClick={handleSave} style={{marginTop: '1em'}}>
                    {task ? 'Update Task' : 'Add Task'}
                </IonButton>
            </IonContent>
        </IonModal>
    );
};

export default TaskModal;