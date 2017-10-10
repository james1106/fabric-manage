/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 09/05/2017
 *
 */

import React, { Component } from 'react';
import { Field, reduxForm, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import { fetchChainCodeInfo } from '../actions/chaincode';
import ChainsSelector from './chains_selector';

class ChainCodeQuery extends Component {
  constructor(props) {
    super(props);
    this.state = { error:null, spin:false };
  }

  componentWillMount() {
  }

  componentWillReceiveProps(props) {
    if(!props.selectedItem) {
      this.setState({error:null});
    }
  }

  handleFormSubmit({ func,args, chain }) {
    if(!this.props.selectedItem) return;

    const { name, version } = this.props.selectedItem
    console.log({ chain, name, version, func,args })

    if(chain && name && version && func && args) {
      this.setState({ spin:true });
      this.props.fetchChainCodeInfo({ chain, name, version, func,args }, err => {
        this.setState({ error: err ? err : null, spin:false });
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

  renderInfo() {
    if(this.props.item == null) return <div></div>;

    const { payload, peer, txid } = this.props.item;

    return (<table className="table table-bordered">
      <tbody>
      <tr>
        <th>Payload</th>
        <td>{payload}</td>
      </tr>
      <tr>
        <th>节点</th>
        <td>{peer}</td>
      </tr>
      <tr>
        <th>txid</th>
        <td>{txid}</td>
      </tr>
      </tbody>
    </table>)
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
    renderFieldfunc({ input, label, type, meta: { touched, error } }) {
        return (
            <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
              <label className="lab">{label}</label>
              <input {...input} placeholder={label} type="type" className="form-control" />
              <div className="help-block with-errors">{touched && error ? error : ''}</div>
            </div>
        )
    }

  renderTd( key ) {
    const { selectedItem } = this.props;
    if(!selectedItem) return <td></td>;
    return (<td>{selectedItem[key]}</td>)
  }

  render() {
    const { handleSubmit} = this.props;
    return (
      <div>
        <div className="">
          <div className="">
            {this.renderAlert()}
            <table className="table table-bordered">
              <tbody>
              <tr><th>名称</th><th>版本号</th><th>语言</th></tr>
              <tr>
                {this.renderTd('name')}
                {this.renderTd('version')}
                {this.renderTd('lang')}
              </tr>
              </tbody>
            </table>
            <form className="form-signin" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
              <Field name="chain" component={ChainsSelector} className="form-control" label="选择链"/>
              <Field name="func" component={this.renderFieldfunc.bind(this)} type="text"  label="函数名"/>
              <Field name="args" component={this.renderField.bind(this)} type="text"  label="参数" />
              <div className="row">
                <div className="col-xs-8">
                  {this.state.spin?'查询中...':''}
                </div>
                <div className="col-xs-4">
                  <button type="submit" className="btn btn-primary pull-right"><i className={`fa fa-spinner fa-spin ${this.state.spin?'':'hidden'}`}></i> 提交 </button>
                </div>
              </div>
            </form>
            { this.renderInfo() }
          </div>
        </div>
      </div>
    );
  }
}


const validate = values => {
  const errors = {};

  if(!values.args) {
    errors.args = '参数不能为空';
  }

  if(!values.chain) {
    errors.chain = '请选择链';
  }

  return errors
};

const selector = formValueSelector('executeChainCodeForm') ;
function mapStateToProps(state) {
  return {
    item: state.chainCode.item,
    chains: state.chain.chainList,
    selectedChain:selector(state, 'chain')
  };
}

const reduxChainCodeQueryForm = reduxForm({
  form: 'executeChainCodeForm',
  validate
})(ChainCodeQuery);

export default connect(mapStateToProps, { fetchChainCodeInfo })(reduxChainCodeQueryForm);