import * as React from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';

export class Crumb extends React.Component<RouteComponentProps<any>, any> {
    render() {
        return (
            <div>
                <Link to="/packages">
                    <i
                        className="fa fa-arrow-circle-left"
                        style={{marginRight: '10px', fontSize: '16px', cursor: 'pointer'}}/>
                    <h4 style={{marginTop: '0', display: 'inline-block'}}>Packages</h4>
                </Link>
            </div>
        );
    }
}