import * as React from 'react';
import {AssignedPackageModel} from "../model";

interface NarrativeRowOwnProps {
    model: AssignedPackageModel
    level: number
}
type NarrativeRowViewerProps = NarrativeRowOwnProps;

export default class NarrativeRow extends React.Component<NarrativeRowViewerProps, any> {

    render() {
        const { model, level } = this.props;
        const { Narrative } = model;
        const indentPx = (level + 1) * 20;

        return (
            <div style={{paddingLeft: indentPx + 'px'}} className="narrative_row">
                - {Narrative}
            </div>
        )
    }
}