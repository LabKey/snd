import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux'
import { Route, RouteComponentProps, RouteProps, Switch } from 'react-router-dom';

import { AppModel } from './model'
import * as actions from './actions'
import { UserModel } from '../SignIn/model'
import { SignIn } from '../SignIn/SignIn'

import { CrumbRoutes, Routes } from '../../routing/Routes'
const styles = require<any>('./App.css');

interface AppOwnProps extends RouteComponentProps<{}> {}
interface AppStateDispatch {
    dispatch?: Dispatch<any>

    dismissWarning?: () => any
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
        dismissWarning: () => dispatch(actions.resetAppError())
    }
}

export class AppImpl extends React.Component<AppProps, {}> {

    static getMessageTypes(app: AppModel): {
        alertClassName: string
        alertType: string
    } {
        let alertClassName,
            alertType;

        if (app.isError === true) {
            alertClassName = 'alert-danger';
            alertType = 'alert';
        }
        else if (app.isWarning) {
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
        const { app } = this.props;

        if (app.message) {
            const alerts = AppImpl.getMessageTypes(app);
            const { alertClassName, alertType } = alerts;

            return (
                <div className="app-error">
                    <div className={[styles['alert-dismiss'], alertClassName].join(' ')}>
                        <i className='fa fa-times' onClick={this.props.dismissWarning}/>
                    </div>
                    <div className={['alert', alertClassName].join(' ')} role={alertType}>
                        {app.message}
                    </div>
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