import * as React from 'react';
import { Panel } from 'react-bootstrap';

import { RouteComponentProps } from 'react-router-dom';



export class LandingPageImpl extends React.Component<RouteComponentProps<any>, any> {

    render() {
        return (
            <Panel>
                Hi, I'm your landing page
            </Panel>
        )
    }
}

export const LandingPage = LandingPageImpl;