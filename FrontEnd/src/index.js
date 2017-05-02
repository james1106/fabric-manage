import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware, compose } from 'redux';
import { Route, BrowserRouter, Switch } from 'react-router-dom';
import reduxThunk from 'redux-thunk';

import reducers from './reducers';
import { AUTH_USER } from './actions/types';

import Welcome from './components/welcome';
import NavTop from './components/common/header';
import NavSide from './components/common/nav_side';
import Footer from './components/common/footer';
import Signout from './components/auth/signout';
import Signin from './components/auth/signin';
import PeerList from './components/peer_list';
import PeerStatus from './components/peer_status';
import UserList from './components/user_list';

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
            <Route path="/peer/:id/status" component={PeerStatus} />
            <Route path="/peer" component={PeerList} />
            <Route path="/users" component={UserList} />
            <Route path="/" component={Welcome} />
          </Switch>
        </div>
        <Footer/>
      </div>
    </BrowserRouter>
  </Provider>
  , document.querySelector('.wrapper'));
