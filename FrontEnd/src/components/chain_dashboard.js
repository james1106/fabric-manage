/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import BlockList from './block_list';
import ChainInfo from './chain_info';

class ChainDashboard extends Component {
  constructor() {
    super();
  }

  render() {
    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-sm-8 col-xs-12">
              <BlockList/>
            </div>

            <div className="col-sm-4 col-xs-12">
              <ChainInfo/>
            </div>
          </div>
        </section>
      </div>)
  }
}

export default ChainDashboard;