import * as React from 'react';
import { connect } from 'react-redux';
import { Route, RouteComponentProps, RouteProps, Switch } from 'react-router-dom';

import { Routes } from '../../routing/Routes'

interface AppOwnProps extends RouteComponentProps<{}> {}
interface AppStateProps {}

type AppProps = AppOwnProps & AppStateProps;


function mapStateToProps(state: any) {
    return {

    }
}
export class AppImpl extends React.Component<AppProps, any> {
    render() {
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