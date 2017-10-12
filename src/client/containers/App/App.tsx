import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux'
import { Route, RouteComponentProps, RouteProps, Switch } from 'react-router-dom';

import { AppMessage, AppModel } from './model'
import * as actions from './actions'
import { UserModel } from '../SignIn/model'
import { SignIn } from '../SignIn/SignIn'

import { CrumbRoutes, Routes } from '../../routing/Routes'
const styles = require<any>('./App.css');

interface AppOwnProps extends RouteComponentProps<{}> {}
interface AppStateDispatch {
    dispatch?: Dispatch<any>

    dismissWarning?: (id?: number) => any
}
interface AppStateProps {
    dispatch?: Dispatch<any>
    app?: AppModel
    user?: UserModel
}

type AppProps = AppOwnProps & AppStateProps & AppStateDispatch;

function mapStateToProps(state: APP_STATE_PROPS) {
    return {
        app: state.app,
        user: state.user
    }
}

function mapDispatchToProps(dispatch: Dispatch<any>): AppStateDispatch {
    return {
        dismissWarning: (id?: number) => dispatch(actions.resetAppError(id))
    }
}

export class AppImpl extends React.Component<AppProps, {}> {

    static getMessageTypes(msg: AppMessage): {
        alertClassName: string
        alertType: string
    } {
        let alertClassName,
            alertType;

        if (msg.role === 'error') {
            alertClassName = 'alert-danger';
            alertType = 'alert';
        }
        else if (msg.role === 'warning') {
            alertClassName = 'alert-warning';
            alertType = 'warning';
        }
        else {
            alertClassName = 'alert-success';
            alertType = 'success';
        }

        return {
            alertClassName,
            alertType
        }
    }

    renderMessage() {
        const { app, dismissWarning } = this.props;

        if (app.messages && app.messages.length) {
            return (
                <div className='app-alert-container'>
                    {app.messages.map(msg => {
                        const alerts = AppImpl.getMessageTypes(msg);
                        const { alertClassName, alertType } = alerts;
                        return (
                            <div className="app-error" key={msg.message + msg.id}>
                                <div className={[styles['alert-dismiss'], alertClassName].join(' ')}>
                                    <i className='fa fa-times' onClick={() => dismissWarning(msg.id)}/>
                                </div>
                                <div className={['alert', alertClassName].join(' ')} role={alertType}>
                                    {msg.message}
                                </div>
                            </div>
                        )
                    })}
                </div>
            );
        }
    }

    render() {
        const { user } = this.props;

        if (!user.isSignedIn) {
            return (
                <div>
                    <SignIn/>
                </div>
            );
        }

        return (
            <div className={styles['content-wrapper']}>
                <div className="container">
                    <Switch>
                        {CrumbRoutes.map((route: RouteProps, index: number) => {
                            return <Route
                                key={index}
                                path={route.path}
                                exact={route.exact}
                                component={route.component}/>;
                        })}
                    </Switch>
                    {this.renderMessage()}
                    <Switch>
                        {Routes.map((route: RouteProps, index: number) => {
                            return <Route
                                key={index}
                                path={route.path}
                                exact={route.exact}
                                component={route.component}/>;
                        })}
                    </Switch>
                </div>
            </div>
        )
    }
}

export const App = connect<any, AppStateDispatch, AppProps>(
    mapStateToProps,
    mapDispatchToProps
)(AppImpl);