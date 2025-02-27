import { PluginListenerHandle } from "@capacitor/core";
import { ConnectionStatus, Network } from "@capacitor/network";
import { useEffect, useState } from "react";

const initialState = {
    connected: false,
    connectionType: 'unknown',
};

export const useNetwork = () => {
    const [networkStatus, setNetworkStatus] = useState(initialState);

    useEffect(() => {
        let handler: PluginListenerHandle;
        let canceled = false;

        async function registerNetworkStatusChange() {
            handler = await Network.addListener('networkStatusChange', handleNetworkStatusChange);
            const status = await Network.getStatus();
            handleNetworkStatusChange(status);
        }

        registerNetworkStatusChange();

        return () => {
            canceled = true;
            handler?.remove();
        }

        function handleNetworkStatusChange(status: ConnectionStatus) {
            console.log('Network status change', status);
            if (!canceled) {
                setNetworkStatus(status);
            }
        }
    }, []);

    return { networkStatus };
};