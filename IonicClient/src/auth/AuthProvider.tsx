import React, { useCallback, useEffect, useState } from "react";
import { getLogger } from "../core";
import PropTypes from 'prop-types';
import { login as loginApi } from './authApi';
import { Preferences } from "@capacitor/preferences";

const log = getLogger('AuthProvider');

type LoginFn = (username?: string, password?: string) => void;
type LogoutFn = () => void;

export interface AuthState {
    authenticationError: Error | null;
    isAuthenticated: boolean;
    isAuthenticating: boolean;
    login?: LoginFn;
    logout?: LogoutFn;
    pendingAuthentication?: boolean;
    username?: string;
    password?: string;
    token: string;
}

const initialState: AuthState = {
    isAuthenticated: false,
    isAuthenticating: false,
    authenticationError: null,
    pendingAuthentication: false,
    token: '',
};

export const AuthContext = React.createContext<AuthState>(initialState);

interface AuthProviderProps {
    children: PropTypes.ReactNodeLike,
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [state, setState] = useState<AuthState>(initialState);
    const { isAuthenticated, isAuthenticating, authenticationError, pendingAuthentication, token } = state;
    
    const login = useCallback<LoginFn>(loginCallback, []);
    const logout = useCallback<LogoutFn>(logoutCallback, []);
    
    useEffect(() => {
        checkTokenOnStart();
    }, []);
    useEffect(authenticationEffect, [pendingAuthentication]);
    const value = { isAuthenticated, login, logout, isAuthenticating, authenticationError, token };
    log('render');
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );

    function loginCallback(username?: string, password?: string): void {
        log('login');
        setState({
            ...state,
            pendingAuthentication: true,
            username,
            password
        });
    }

    async function logoutCallback() {
        log('logout');
        await Preferences.remove({ key: 'authToken' });
        setState({ ...initialState });
    }

    function authenticationEffect() {
        let canceled = false;
        authenticate();
        return () => {
            canceled = true;    
        }
        
        async function authenticate() {
            if (!pendingAuthentication) {
                log('authenticate, !pendingAthentication, return');
                return;
            }
            try {
                log('authenticate...');
                setState({
                    ...state,
                    isAuthenticating: true,
                });
                const { username, password } = state;
                const { token } = await loginApi(username, password);
                if (canceled) {
                    return;
                }
                log('authenticate succeeded');
                await Preferences.set({ key: 'authToken', value: token });
                setState({
                    ...state,
                    token,
                    pendingAuthentication: false,
                    isAuthenticated: true,
                    isAuthenticating: false,
                });
            } catch (error) {
                if (canceled) {
                    return;
                }
                log('authenticate failed', error);
                setState({
                    ...state,
                    authenticationError: error as Error,
                    pendingAuthentication: false,
                    isAuthenticating: false,
                });
            }
        }
    }

    async function checkTokenOnStart() {
        const { value } = await Preferences.get({ key: 'authToken' });
        if (value) {
            setState({ ...state, token: value, isAuthenticated: true });
        }
    }
}