import * as React from 'react';
import { connect } from 'react-redux';
import { Route, RouteComponentProps, RouteProps, Switch } from 'react-router-dom';


function mapStateToProps(state: any) {
    return {

    }
}
export class AppImpl extends React.Component<any, any> {
    render() {
        return (
            <div>
                Hi, I'm your home page....eventually
            </div>
        )
    }
}

export const App = connect(mapStateToProps)(AppImpl);