import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';


import { EditCategories } from '../containers/Categories/Forms/Edit'
import { LandingPage } from '../containers/LandingPage/LandingPage'
import { PackageFormContainer } from '../containers/Packages/Forms/PackageFormContainer'
import { PackageViewer } from '../containers/Packages/Forms/PackageViewer'

import { NotFound } from '../components/NotFound/NotFound'


export interface RouteProps {
    component: React.ComponentClass<RouteComponentProps<any>>;
    exact?: boolean;
    path?: string;
}


export const Routes: Array<RouteProps> = [
    {
        component: LandingPage,
        exact: true,
        path: '/',
    },

    {
        component: PackageViewer,
        exact: true,
        path: '/packages',
    },

    {
        component: PackageFormContainer,
        exact: true,
        path: '/packages/new',
    },

    {
        component: PackageFormContainer,
        exact: true,
        path: '/packages/edit/:id',
    },

    {
        component: PackageFormContainer,
        exact: true,
        path: '/packages/view/:id',
    },

    {
        component: PackageFormContainer,
        exact: true,
        path: '/packages/clone/:id',
    },

    {
        component: EditCategories,
        exact: true,
        path: '/categories',
    },

    {
        component: EditCategories,
        exact: true,
        path: '/categories/edit',
    },

    {
        component: NotFound
    }
];
