import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom';

export class EditCategories extends React.Component<RouteComponentProps<any>, any> {
    render() {
        return (
            <div>
                <Panel>
                    Hi I'm your new Edit Categories Page
                </Panel>
            </div>
        );
    }
}