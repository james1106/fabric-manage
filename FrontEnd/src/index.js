import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware, compose } from 'redux';
import { Route, BrowserRouter, Switch } from 'react-router-dom';
import reduxThunk from 'redux-thunk';

import reducers from './reducers';
import { AUTH_USER } from './actions/types';

import RequireAuth from './components/auth/require_auth';
import PrivateRoute from './components/auth/private_route';
import Welcome from './components/welcome';
import NavTop from './components/common/header';
import NavSide from './components/common/nav_side';
import Footer from './components/common/footer';
import Signout from './components/auth/signout';
import Signin from './components/auth/signin';
import PeerList from './components/peer_list';
import PeerStatus from './components/peer_status';
import PeerDetail from './components/peer_detail';
import UserList from './components/user_list';
import ChainDashboard from './components/chain_dashboard';
import BlockInfo from './components/block_info';
import ChainCodeList from './components/chaincode_list';
import ChainCodeUpload from './components/chaincode_upload';
import UserProfile from './components/user_profile';

const createStoreWithMiddleware = compose(
  applyMiddleware(reduxThunk),
  window.devToolsExtension ? window.devToolsExtension() : f => f
)(createStore);
const store = createStoreWithMiddleware(reducers);

const token = localStorage.getItem('token');
// If token exist, singin automatic
if (token) {
  store.dispatch({ type: AUTH_USER });
}

ReactDOM.render(
  <Provider store={store}>
    <BrowserRouter>
      <div>
        <NavTop />
        <NavSide/>
        <div className="content-wrapper">
          <Switch>
            <Route path="/signout" component={Signout} />
            <Route path="/signin" component={Signin} />
            <PrivateRoute path="/peer/:id/status" component={PeerStatus} />
            <PrivateRoute path="/peer/:id" component={PeerDetail} />
            <PrivateRoute path="/peer" component={PeerList} />
            <PrivateRoute path="/chaincode/add" component={ChainCodeUpload} />
            <PrivateRoute path="/chaincode" component={ChainCodeList} />
            <PrivateRoute path="/chain/block/:id" component={BlockInfo} />
            <PrivateRoute path="/chain" component={ChainDashboard} />
            <PrivateRoute path="/users" component={UserList}/>
            <PrivateRoute path="/profile" component={UserProfile} />
            <Route path="/" component={Welcome} />
          </Switch>
        </div>
        <Footer/>
      </div>
    </BrowserRouter>
  </Provider>
  , document.querySelector('.wrapper'));
