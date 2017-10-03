import * as React from 'react';
import {AssignedPackageModel} from "../model";

interface NarrativeRowOwnProps {
    SubPackages: Array<AssignedPackageModel>
    Narrative: string
}
type NarrativeRowViewerProps = NarrativeRowOwnProps;

export default class NarrativeRow extends React.Component<NarrativeRowViewerProps, any> {

    render() {

        const { Narrative, SubPackages } = this.props;

        return (
            <div style={{margin:'0 0 0 20px'}} key={Narrative} className="narrative_row">
                - {Narrative}
                {SubPackages.map((subPackage) =>
                    <NarrativeRow
                        Narrative={subPackage.Narrative}
                        SubPackages={subPackage.SubPackages}/>
                )}
            </div>
        )
    }
}