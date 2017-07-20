/**
 * Created by oxchain on 2017/7/20.
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
class Chainwatch extends Component {
    constructor(props) {
        super(props);
        this.state = {

        };
    }
    render() {
        return (
            <div >
                <div className="chainiframe">
                    <iframe  src="https://datav.aliyun.com/share/85603cadebbedbee1920046c13b971ae" frameborder="0"></iframe>
                </div>

            </div>)
    }
}

export default connect(null,{})(Chainwatch);