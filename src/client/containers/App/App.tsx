import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux'
import { Route, RouteComponentProps, RouteProps, Switch } from 'react-router-dom';

import { UserModel } from '../SignIn/model'
import { SignIn } from '../SignIn/SignIn'

import { Routes } from '../../routing/Routes'
import { APP_STATE_PROPS } from '../../reducers/index'

interface AppOwnProps extends RouteComponentProps<{}> {}
interface AppStateProps {
    dispatch?: Dispatch<any>
    user?: UserModel
}

type AppProps = AppOwnProps & AppStateProps;


function mapStateToProps(state: APP_STATE_PROPS) {
    return {
        user: state.user
    }
}
export class AppImpl extends React.Component<AppProps, any> {
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
            <div>
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
        )
    }
}

export const App = connect<any, any, AppProps>(mapStateToProps)(AppImpl);