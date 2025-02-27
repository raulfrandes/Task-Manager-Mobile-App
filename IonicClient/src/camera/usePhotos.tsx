import { useCallback, useEffect, useState } from "react";
import { useCamera } from "./useCamera";
import { useFilesystem } from "./useFilesystem";
import { usePreferences } from "../pages/usePreferences";

export interface MyPhoto {
    taskId: number;
    webviewPath?: string;
}

const PHOTOS = 'photos';

export function usePhotos() {
    const { getPhoto } = useCamera();
    const { readFile, writeFile } = useFilesystem();
    const { get, set } = usePreferences();
    const [photos, setPhotos] = useState<MyPhoto[]>([]);

    return {
        takePhoto,
        loadPhoto
    }

    async function takePhoto(taskId: number) {
        const data = await getPhoto();
        const filepath = taskId + '.jpeg';
        await writeFile(filepath, data.base64String!);
        const webviewPath = `data:image/jpeg;base64,${data.base64String}`;
        const newPhoto = { taskId, webviewPath };
        const newPhotos = [newPhoto, ...photos];
        await set(PHOTOS, JSON.stringify(newPhotos.map(p => ({ taskId: p.taskId }))));
        setPhotos(newPhotos);
        return newPhoto;
    }

    function loadPhoto(taskId: number) {
        loadSavedPhoto(taskId);

        async function loadSavedPhoto(taskId: number) {
            const savedPhotoString = await get(PHOTOS);
            const savedPhotos = (savedPhotoString ? JSON.parse(savedPhotoString) : []) as MyPhoto[];
            console.log('load', savedPhotos);
            let loadedPhoto;
            for (let photo of savedPhotos) {
                if (photo.taskId === taskId) {
                    const data = await readFile(photo.taskId.toString() + '.jpeg');
                    photo.webviewPath = `data:image/jpeg;base64,${data}`;
                    loadedPhoto = photo;
                }
            }
            setPhotos(savedPhotos);
            return loadPhoto;
        }
    }
}