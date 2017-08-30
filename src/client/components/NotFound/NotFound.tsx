import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom';

export class NotFound extends React.Component<RouteComponentProps<any>, any> {
    render() {
        return (
            <div>
                <Panel>
                    <div>No page found at:</div>
                    <div className="not-found-message">
                        <em>{this.props.location.pathname}</em>
                    </div>
                </Panel>
            </div>
        );
    }
}