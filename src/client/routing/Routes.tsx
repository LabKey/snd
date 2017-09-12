import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';


import { EditCategories } from '../containers/Categories/Forms/Edit'
import { LandingPage } from '../containers/LandingPage/LandingPage'
import { PackageFormContainer } from '../containers/Packages/Forms/PackageFormContainer'
import { PackageSearch } from '../containers/Packages/Forms/PackageSearch'

import { NotFound } from '../components/NotFound/NotFound'
import { Crumb } from '../components/Crumb/Crumb'


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
        component: PackageSearch,
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

export const CrumbRoutes: Array<RouteProps> = [
    {
        component: null,
        exact: true,
        path: '/',
    },

    {
        component: null,
        exact: true,
        path: '/packages',
    },

    {
        component: Crumb,
        path: '/*',
    },
];