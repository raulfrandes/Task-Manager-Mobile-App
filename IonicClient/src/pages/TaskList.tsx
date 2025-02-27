import { RouteComponentProps } from "react-router";
import { getLogger } from "../core";
import { useContext, useEffect, useState } from "react";
import { TaskContext } from "../components/TaskProvider";
import { IonBadge, IonContent, IonFab, IonFabButton, IonHeader, IonIcon, IonInfiniteScroll, IonInfiniteScrollContent, IonInput, IonItem, IonList, IonLoading, IonPage, IonSelect, IonSelectOption, IonTitle, IonToolbar } from "@ionic/react";
import Task from "../components/Task";
import TaskDetail from "../components/TaskDetail";
import { add, warning } from 'ionicons/icons';
import TaskModal from "../components/TaskModal";
import { useNetwork } from "./useNetwork";
import { AuthContext } from "../auth/AuthProvider";
import { Preferences } from "@capacitor/preferences";
import { throttle } from 'lodash';
import { AnimatedTitle } from "../animation/AnimatedTitle";

const log = getLogger('TaskList');

const TaskList: React.FC = () => {
  const { tasks, fetching, fetchingError, selectedTask, setSelectedTask, fetchMoreTasks, hasMore, totalTasks, setSearchQuery, setFilterCompleted, unsyncedCount } = useContext(TaskContext);
  const { networkStatus } = useNetwork();
  const { logout } = useContext(AuthContext);
  const [showTaskModal, setShowTaskModal] = useState<boolean>(false);
  const [searchText, setSearchText] = useState<string>("");

  const handleSearchChange = (e: any) => {
    const newValue = e.detail.value;
    setSearchText(newValue);
    setSearchQuery?.(newValue);
  }

  log('render');

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <AnimatedTitle />
          {(unsyncedCount > 0) && (
            <IonBadge color="warning">
              {unsyncedCount} {unsyncedCount > 1 ? 'tasks': 'task'} will be saved when back online
            </IonBadge>
          )}
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonHeader collapse="condense">
          <IonToolbar>
            {/* <AnimatedTitle /> */}
          </IonToolbar>
        </IonHeader>

        <div style={{ padding: '10px', color: networkStatus.connected ? 'green' : 'red' }}>
          Network status: {networkStatus.connected ? 'Online' : 'Offline'} ({networkStatus.connectionType})
        </div>

        <IonItem>
          <IonInput 
            placeholder="Search tasks..."
            value={searchText}
            onIonChange={handleSearchChange}
          />
        </IonItem>

        <IonItem>
          <IonSelect placeholder="Filter by Completion" onIonChange={(e) => {setFilterCompleted?.(e.detail.value)}}>
            <IonSelectOption value="all">All</IonSelectOption>
            <IonSelectOption value="completed">Completed</IonSelectOption>
            <IonSelectOption value="incompleted">Incompleted</IonSelectOption>
          </IonSelect>
        </IonItem>

        <IonLoading isOpen={fetching} message={"Fetching tasks"} />

        {selectedTask ? (
          <TaskDetail task={selectedTask} onBack={() => setSelectedTask?.(null)} />
        ) : (
          <IonList>
            {tasks && tasks.map(({ id, title, description, dueDate, priority, completed, photoUrl, latitude, longitude }) => (
              <Task key={id} id={id} title={title} description={description} dueDate={dueDate} priority={priority} completed={completed} photoUrl={photoUrl} latitude={latitude} longitude={longitude} onSelect={setSelectedTask!} />
            ))}
            <div style={{ height: "80px" }}></div>
          </IonList>
        )}

        {fetchingError && ( 
          <div>{fetchingError.message || 'Failed to fetch tasks'}</div>
        )}

        <IonInfiniteScroll
          threshold="150px"
          disabled={!hasMore}
          onIonInfinite={(e: CustomEvent<void>) => fetchMoreTasks?.(e)}
        >
          <IonInfiniteScrollContent 
            loadingText="Loading more tasks..."
          />
        </IonInfiniteScroll>

        <IonFab vertical="bottom" horizontal="end" slot="fixed">
          <IonFabButton onClick={() => setShowTaskModal(true)}>
            <IonIcon icon={add} />
          </IonFabButton>
        </IonFab>

        <IonFab vertical="bottom" horizontal="start" slot="fixed">
          <IonFabButton onClick={() => logout?.()}>
            Logout
          </IonFabButton>
        </IonFab>

        <TaskModal show={showTaskModal} task={null} onClose={() => setShowTaskModal(false)}></TaskModal>
      </IonContent>
    </IonPage>
  );
};

export default TaskList;

