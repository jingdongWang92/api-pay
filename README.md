

### 1.  微信扫码支付
- 请求路径 `/wxpay`
- 请求方式 `POST`
- 请求参数说明
<pre>
{
  "out_trade_no": "201811080001",  
  "trade_amount": 1, //单位: 元
  "body": "地图工具企业版",
  "subject": "地图工具企业版",
  "product_id": "123456789",
}
</pre>
响应
<pre>
{
    "error": false,
    "message": "",
    "payload": {
        "nonce_str": "6y3Ey3HlioBgm0nI",
        "code_url": "weixin://wxpay/bizpayurl?pr=hG0kt1L", //二维码链接
        "appid": "wx100d268a6a1246e7",
        "sign": "0C2F456BC3C77318BC16B533D100BFEA",
        "trade_type": "NATIVE",
        "return_msg": "OK",
        "result_code": "SUCCESS",
        "mch_id": "1460124702",
        "return_code": "SUCCESS",
        "prepay_id": "wx201711081620433c8447f4600696133669"
    },
    "meta": null
}
</pre>

### 2. 支付宝网页支付
- 请求路径 `/alipay/page-pay`
- 请求方式 `GET`
- 请求参数说明
<pre>
{
  "out_trade_no": "201811080001",  
  "trade_amount": 1, //单位: 元
  "body": "地图工具企业版",
  "subject": "地图工具企业版"
}
</pre>
