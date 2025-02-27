export interface TaskProps {
    id?: number;
    title: string;
    description: string;
    dueDate: string;
    priority: number;
    completed: boolean;
    photoUrl?: string;
    latitude?: number;
    longitude?: number;
}