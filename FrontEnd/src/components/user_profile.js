/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Date: 12/06/2017
 *
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import jwtDecode from 'jwt-decode';


export default class UserProfile extends Component {
  constructor(props) {
    super(props);

    const { authority } = jwtDecode(localStorage.getItem('token'));
    this.state = {
      isAdmin: authority === 'MANAGE',
      currentUser: JSON.parse(localStorage.getItem('user'))
    };
    console.log(this.state.currentUser)
  }

  render() {
    const { ca, certificate, msp, affiliation, username } = this.state.currentUser;
    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">用户信息</h3>
                </div>
                <div className="box-body table-responsive">
                  <div className="panel panel-default">
                    <div className="panel-heading">证书信息</div>
                    <div className="panel-body">
                      <dl className="dl-horizontal">
                        <dt>机构</dt>
                        <dd>{affiliation}</dd>
                        <dt>msp</dt>
                        <dd>{msp}</dd>
                        <dt>CA</dt>
                        <dd>{ca}</dd>
                        <dt>证书</dt>
                        <dd>{certificate}</dd>
                      </dl>
                    </div>
                  </div>
                </div>
                <div className="box-footer clearfix">
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>)
  }
}