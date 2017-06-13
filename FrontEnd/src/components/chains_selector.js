/**
 * oxchain
 *
 *
 * Author: Jun
 * Date: 13/06/2017
 *
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchChainList } from '../actions/chain';


class ChainsSelector extends React.Component {

  componentWillMount() {
    this.props.fetchChainList();
  }

  renderSelectOptions() {
    if(!this.props.chains) return null;
    return this.props.chains.map((row, idx) => {
      return <option value={row.name} key={idx}>{row.name}</option>
    });
  }

  render() {
    const { input, label, meta: { touched, error } } = this.props;
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <label>{label}</label>
        <select {...input} className="form-control">
          <option></option>
          {this.renderSelectOptions()}
        </select>
        <div className="help-block with-errors">{touched && error ? error : ''}</div>
      </div>
    );
  }
}

//ChainsSelector.propTypes = {
//  people: React.PropTypes.array,
//  input: React.PropTypes.object,
//  label: React.PropTypes.string
//};

function mapStateToProps(state) {
  return {
    chains: state.chain.chainList
  };
}

export default connect(mapStateToProps, { fetchChainList })(ChainsSelector);