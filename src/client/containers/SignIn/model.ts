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

export class UserModel implements UserProps {
    avatar: string = LABKEY.contextPath + '/_images/defaultavatar.png';
    canDelete: boolean = false;
    canDeleteOwn: boolean = false;
    canInsert: boolean = false;
    canUpdate: boolean = false;
    canUpdateOwn: boolean = false;
    displayName: string = 'guest';
    email: string = undefined;
    id: number = 0;
    isAdmin: boolean = false;
    isDeveloper: boolean = false;
    isGuest: boolean = true;
    isRootAdmin: boolean = false;
    isSignedIn: boolean = false;
    isSystemAdmin: boolean = false;
    phone: string = null;

    constructor(props?: Partial<UserModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}