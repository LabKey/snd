/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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