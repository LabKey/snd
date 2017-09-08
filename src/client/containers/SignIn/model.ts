// SignIn
export interface SignInProps {
    email: string;
    password: string;
    remember: boolean;
}

// User
interface UserProps {
    avatar: string;
    canDelete: boolean;
    canDeleteOwn: boolean;
    canInsert: boolean;
    canUpdate: boolean;
    canUpdateOwn: boolean;
    displayName: string;
    email: string;
    id: number;
    isAdmin: boolean;
    isDeveloper: boolean;
    isGuest: boolean;
    isRootAdmin: boolean;
    isSignedIn: boolean;
    isSystemAdmin: boolean;
    phone: string;
}

export const defaultUser: UserProps = {
    avatar: LABKEY.contextPath + '/_images/defaultavatar.png',
    canDelete: false,
    canDeleteOwn: false,
    canInsert: false,
    canUpdate: false,
    canUpdateOwn: false,
    displayName: 'guest',
    email: 'guest',
    id: 0,
    isAdmin: false,
    isDeveloper: false,
    isGuest: true,
    isRootAdmin: false,
    isSignedIn: false,
    isSystemAdmin: false,
    phone: null
};

export class UserModel implements UserProps {
    avatar: string;
    canDelete: boolean;
    canDeleteOwn: boolean;
    canInsert: boolean;
    canUpdate: boolean;
    canUpdateOwn: boolean;
    displayName: string;
    email: string;
    id: number;
    isAdmin: boolean;
    isDeveloper: boolean;
    isGuest: boolean;
    isRootAdmin: boolean;
    isSignedIn: boolean;
    isSystemAdmin: boolean;
    phone: string;

    constructor(values: UserProps = defaultUser) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}