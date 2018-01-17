import * as React from 'react';
import { Link } from 'react-router-dom'
import { OverlayTrigger, Tooltip, TooltipProps } from 'react-bootstrap'
import { QueryProjectModel } from '../../containers/Projects/model'

interface ProjectRowProps {
    data: QueryProjectModel
    dataId: number
    handleDelete?: (id, rev, objId) => any
}

interface ProjectRowStateProps {
    isHover: boolean
}

export class ProjectRow extends React.Component<ProjectRowProps, ProjectRowStateProps> {

    static generateToolTip(tooltip: string, id: string): React.ReactElement<TooltipProps> {
        return <Tooltip id={id}>{tooltip}</Tooltip>;
    }

    constructor(props: ProjectRowProps) {
        super(props);

        this.state = {
            isHover: false
        };

        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
    }

    handleMouseEnter() {
        this.setState({isHover: true});
    }

    handleMouseLeave() {
        this.setState({isHover: false});
    }

    render() {
        const { handleDelete } = this.props;
        const { Description, HasEvent, ProjectId, RevisionNum, ObjectId, Latest } = this.props.data;
        const { isHover } = this.state;
        const idRev = ProjectId.value + '|' + RevisionNum.value;

        // HasEvent is a string boolean
        const canDelete = (HasEvent.value !== 'true' && Latest === true);
        const canRevise = (Latest === true);

        // todo: URLs should be more resilient
        return (
            <div
                className="project_viewer__result clearfix project-row"
                data-projectId={ProjectId.value}
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}>
                <div className="result-icons pull-left">
                    <OverlayTrigger overlay={ProjectRow.generateToolTip('View', ProjectId.value)} placement="top">
                        <Link to={'/projects/view/' + idRev} className="project-row_icon">
                            <i className={"fa fa-eye"}/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={ProjectRow.generateToolTip('Edit', ProjectId.value)} placement="top">
                        <Link to={'/projects/edit/' + idRev} className="project-row_icon">
                            <i className="fa fa-pencil"/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={ProjectRow.generateToolTip('Revise', ProjectId.value)} placement="top">
                        {canRevise ?
                            <Link to={'/projects/revise/' + idRev} className="project-row_icon">
                                <i className="fa fa-files-o"/>
                            </Link> :
                            <div className="project-row_icon icon-inline">
                                <i className="fa fa-files-o fa-disabled"/>
                            </div>
                        }
                    </OverlayTrigger>
                </div>
                <div className="pull-left " style={{marginLeft: '10px'}}>
                    {[ProjectId.value, [Description.value, "Revision " + RevisionNum.value].join(', ')].join(' - ')}
                </div>
                {isHover && canDelete ?
                    <div className="pull-right" style={{cursor: 'pointer'}}>
                        <i className="fa fa-times" onClick={() => handleDelete(ProjectId.value, RevisionNum.value, ObjectId.value)}/>
                    </div>
                    : null}

            </div>
        )
    }
}