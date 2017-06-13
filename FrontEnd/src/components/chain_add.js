/**
 * oxchain
 *
 *
 * Author: Jun
 * Date: 12/06/2017
 *
 */

import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import { addChain } from '../actions/chain'

class ChainAdd extends Component {
  constructor(props) {
    super(props);
    this.state = { error:null, spin:false };
  }


  handleFormSubmit({ chainname, config }) {

    if(chainname && config) {
      this.setState({ spin:true });
      this.props.addChain({ chainname, config }, err => {
        this.setState({ error: err ? err : null, spin:false });
        if(typeof this.props.addCallback === 'function') this.props.addCallback(err);
      });
    }
  }

  renderAlert() {
    if (this.state.error) {
      return (
        <div className="alert alert-danger alert-dismissable">
          {this.state.error}
        </div>
      );
    }
  }

  renderField({ input, label, type, meta: { touched, error } }) {
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <label>{label}</label>
        <input {...input} placeholder={label} type={type} className="form-control"/>
        <div className="help-block with-errors">{touched && error ? error : ''}</div>
      </div>
    )
  }

  render() {
    const { handleSubmit} = this.props;
    return (
      <div>
        <div className="">
          <div className="">
            {this.renderAlert()}
            <form className="form-signin" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
              <Field name="chainname" component={this.renderField} type="text"  label="链名称" />
              <Field name="config" component={this.renderField} type="file" label="配置文件" />
              <div className="row">
                <div className="col-xs-8">
                  {this.state.spin?'创建链耗时较长,请耐心等待...':''}
                </div>
                <div className="col-xs-4">
                  <button type="submit" className="btn btn-primary pull-right"><i className={`fa fa-spinner fa-spin ${this.state.spin?'':'hidden'}`}></i> 提交 </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    );
  }
}


const validate = values => {
  const errors = {};

  if(!values.chainname) {
    errors.chainname = '名称不能为空';
  }

  if(!values.config) {
    errors.config = '配置文件不能为空';
  }

  return errors
};


const reduxChainAddForm = reduxForm({
  form: 'addChainForm',
  validate
})(ChainAdd);

export default connect(null, { addChain })(reduxChainAddForm);