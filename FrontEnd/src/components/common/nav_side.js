import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

class NavSide extends  Component {

  renderLinks() {
    if (this.props.authenticated) {
      // show a link to sign out
      return  <li key="nav1"><Link to="/signout"><i className="fa fa-sign-in"></i> <span>退出登录</span></Link></li>
    } else {
      return [
        <li key="nav2"><Link to="/signin"><i className="fa fa-sign-in"></i> <span>登录</span></Link></li>,
      ];
    }
  }

  renderUserInfo() {
    if(this.props.authenticated) {
      //const user = JSON.parse(localStorage.getItem('user'));
      const username= localStorage.getItem('username');
      const avatar = `https://gravatar.com/avatar/oxchain-${username}?s=100&d=retro`;
      return <div className="user-panel">
        <div className="pull-left image">
          <img src={avatar} className="img-circle" alt="User Image" style={{"width":"100px"}} />
        </div>
        <div className="pull-left info">
          <p>{username}</p>
        </div>
      </div>
    } else {
      return <div></div>
    }
  }

  render() {
    return (
      <aside className="main-sidebar">
        <section className="sidebar">
          { this.renderUserInfo() }
          <ul className="sidebar-menu">
            <li className="header">导航</li>
            { this.renderLinks() }
            <li key="nav5"><Link to="/peer"><i className="fa fa-database"></i> <span>节点管理</span></Link></li>
            <li key="nav6"><Link to="/chain"><i className="fa fa-link"></i> <span>区块链</span></Link></li>
            <li key="nav7"><Link to="/contract"><i className="fa fa-bitcoin"></i> <span>合约管理</span></Link></li>
            <li key="nav8"><Link to="/users"><i className="fa fa-users"></i> <span>用户管理</span></Link></li>
          </ul>
        </section>
      </aside>
    );
  }
}

function mapStateToProps(state) {
  return { authenticated: state.auth.authenticated };
}

export default connect(mapStateToProps)(NavSide);