import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom';

export class NewPackage extends React.Component<RouteComponentProps<any>, any> {
    render() {
        return (
            <div>
                <Panel>
                    Hi I'm your new package page
                </Panel>
            </div>
        );
    }
}