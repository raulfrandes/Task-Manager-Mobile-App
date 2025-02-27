import React from 'react';
import { TaskProps } from '../core/TaskProps';
import { IonImg, IonItem, IonLabel, IonThumbnail } from '@ionic/react';
import { usePhotos } from '../camera/usePhotos';

interface TaskPropsExt extends TaskProps {
    onSelect: (task: TaskProps) => void;
}

const Task: React.FC<TaskPropsExt> = ({ id, title, description, dueDate, priority, completed, photoUrl, latitude, longitude, onSelect }) => {
    return(
        <IonItem key={ id } button onClick={() => onSelect({ id, title, description, dueDate, priority, completed, photoUrl, latitude, longitude })}>
            <IonThumbnail slot="start">
                {photoUrl && <IonImg src={photoUrl}/>}    
            </IonThumbnail>
            <IonLabel>{title}</IonLabel>
        </IonItem>
    )
}

export default React.memo(Task);