# 知识产权管理系统 API 文档

## 基础信息

- **Base URL**: `http://localhost:5050`
- **认证方式**: Bearer Token（JWT），登录成功后需在请求头携带 `Authorization: Bearer <token>`
- **Content-Type**: `application/json`
- **响应格式**: 统一 `Result<T>` 结构

### 通用响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 成功，401 未登录，403 权限不足，500/600 业务异常 |
| message | string | 提示信息 |
| data | object / null | 响应数据 |

---

## 一、公开接口

### 1.1 获取图形验证码

```
GET /api/acount/checkCode
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldCheckCodeKey | string | 否 | 旧的验证码 key，传入后会先清除旧验证码 |

**响应 data**

```json
{
  "checkCode": "data:image/png;base64,iVBORw0KGgo...",
  "checkCodeKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| checkCode | string | Base64 编码的算术验证码图片 |
| checkCodeKey | string | 验证码唯一标识，登录/注册时需回传 |

---

### 1.2 用户注册

```
POST /api/acount/register
```

**请求体**

```json
{
  "loginName": "zhangsan",
  "email": "zhangsan@example.com",
  "phoneNumber": "13800138000",
  "password": "123456",
  "checkCodeKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "checkCode": "8"
}
```

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| loginName | string | 是 | 长度 3~30 位 |
| email | string | 是 | 合法邮箱格式 |
| phoneNumber | string | 是 | 中国大陆手机号 |
| password | string | 是 | 长度 6~20 位 |
| checkCodeKey | string | 是 | 验证码接口返回的 key |
| checkCode | string | 是 | 用户输入的验证码结果 |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "注册成功"
}
```

**失败响应**

```json
{
  "code": 500,
  "message": "注册失败:账号已存在",
  "data": null
}
```

---

### 1.3 用户登录

```
POST /api/acount/login
```

**请求体**

```json
{
  "loginName": "zhangsan",
  "phoneNumber": "13800138000",
  "password": "123456",
  "checkCodeKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "checkCode": "8"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginName | string | 是 | 登录账号 |
| phoneNumber | string | 是 | 手机号 |
| password | string | 是 | 密码 |
| checkCodeKey | string | 是 | 验证码 key |
| checkCode | string | 是 | 验证码 |

**成功响应**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibG9naW5OYW1lIjoiemhhbmdzYW4iLCJpYXQiOjE3NTMyMDAwMDAsImV4cCI6MTc1MzI4NjQwMH0.xxx",
    "userId": 1,
    "loginName": "zhangsan",
    "userName": "张三",
    "roles": ["admin"],
    "permissions": ["system:user:list", "system:user:add", "system:user:edit", "system:user:delete"]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT 访问令牌，有效期 24 小时 |
| userId | long | 用户 ID |
| loginName | string | 登录账号 |
| userName | string | 用户昵称 |
| roles | string[] | 角色标识集合（来自 sys_role.role_key） |
| permissions | string[] | 权限标识集合（来自 sys_menu.perms） |

**失败响应**

```json
{
  "code": 500,
  "message": "账号或密码错误",
  "data": null
}
```

> 登录失败统一返回 "账号或密码错误"，不泄露账号是否存在。

---

## 二、需认证接口

> 以下接口需在请求头携带 `Authorization: Bearer <token>`

### 2.1 退出登录

```
POST /api/acount/logout
```

**无请求体**

**成功响应**

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

> 退出后服务端清除 Redis 中的 Token 缓存，该 Token 即刻失效。

---

### 2.2 获取当前用户信息

```
GET /api/acount/me
```

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "loginName": "zhangsan",
    "roles": ["admin"],
    "permissions": ["system:user:list", "system:user:add", "system:user:edit", "system:user:delete"]
  }
}
```

**未登录响应**

```json
{
  "code": 401,
  "message": "未登录",
  "data": null
}
```

---

### 2.3 用户列表

```
GET /api/acount/list
```

> 需要权限：`system:user:list`

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 1,
      "loginName": "zhangsan",
      "userName": "张三",
      "email": "zhangsan@example.com",
      "phoneNumber": "13800138000",
      "status": "0",
      "createTime": "2026-07-21T17:00:00",
      "...": "..."
    }
  ]
}
```

**权限不足响应**

```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

---

### 2.4 上传文件

```
POST /upload
```

> Content-Type: `multipart/form-data`

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件，最大 10MB |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "/files/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf?name=%E5%8E%9F%E5%A7%8B%E6%96%87%E4%BB%B6%E5%90%8D.pdf"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| data | string | 文件访问 URL，后续邮件发送直接传入此值 |

---

### 2.5 查看/下载文件

```
GET /files/{fileId}?name=原始文件名
```

> fileId 格式：`{uuid}.{ext}`，可从上传接口返回的 URL 中提取

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | string | 是 | 路径参数，磁盘上的文件名 |
| name | string | 否 | 查询参数，用于 Content-Disposition 响应头，支持中文 |

**响应**: 直接返回文件流，非 JSON。`Content-Type` 根据文件扩展名自动探测，`Content-Disposition` 为 `inline`（浏览器内预览）。

---

### 2.6 发送邮件

```
POST /api/mail/send
```

> Content-Type: `application/json`
>
> 附件需先通过 `POST /upload` 上传，将返回的 URL 传入 `attachmentUrls`。
> 同时支持两种模式：**普通发送**（`text` + `subject`）和**模板发送**（`templateCode` + `templateData`）。

**请求体**

```json
{
  "to": "receiver@example.com",
  "cc": "cc@example.com,cc2@example.com",
  "subject": "手动指定主题",
  "text": "<p>手动指定正文，支持 <b>HTML</b></p>",
  "templateCode": null,
  "templateData": null,
  "attachmentUrls": [
    "/files/a1b2c3d4.pdf?name=合同.pdf",
    "/files/e5f67890.docx?name=附件二.docx"
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| to | string | 是 | 收件人邮箱，多个用逗号或分号分隔 |
| cc | string | 否 | 抄送人邮箱，多个用逗号或分号分隔 |
| subject | string | 条件 | **普通模式必填**，邮件主题 |
| text | string | 条件 | **普通模式必填**，邮件正文，支持 HTML |
| templateCode | string | 条件 | **模板模式必填**，邮件模板编码 |
| templateData | object | 否 | 模板变量 Map，key 对应模板中的 `${变量名}` |
| attachmentUrls | string[] | 否 | 附件 URL 列表，来自 `POST /upload` 返回值 |

> **两种模式互斥**：传入 `templateCode` 时走模板模式（忽略 `subject`/`text`），否则走普通模式。

**模板模式示例**

```json
{
  "to": "client@example.com",
  "cc": "",
  "templateCode": "DISCLOSURE_CONTACT",
  "templateData": {
    "companyName": "某某科技有限公司",
    "contactPerson": "张三",
    "disclosureDate": "2026-07-23"
  },
  "attachmentUrls": []
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 1,
    "disclosureId": null,
    "internalNo": null,
    "templateId": null,
    "templateCode": null,
    "fromEmail": "sender@qq.com",
    "toEmails": "receiver@example.com",
    "ccEmails": "cc@example.com",
    "subject": "邮件主题",
    "content": "<p>邮件正文</p>",
    "sendStatus": "SUCCESS",
    "errorMessage": null,
    "senderUserId": 1,
    "senderName": "zhangsan",
    "sentAt": "2026-07-23T17:00:00",
    "createTime": "2026-07-23T17:00:00"
  }
}
```

**data 字段说明 (MailSendLog)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 发送记录主键 |
| disclosureId | long/null | 关联的交底 ID |
| internalNo | string/null | 内部编号 |
| templateId | long/null | 使用的模板 ID（模板模式时有值） |
| templateCode | string/null | 使用的模板编码（模板模式时有值） |
| fromEmail | string | 发件人邮箱 |
| toEmails | string | 收件人，逗号分隔 |
| ccEmails | string/null | 抄送人，逗号分隔 |
| subject | string | 实际发送的主题 |
| content | string | 实际发送的正文（HTML） |
| sendStatus | string | `PENDING` / `SUCCESS` / `FAILED` |
| errorMessage | string/null | 失败原因 |
| senderUserId | long | 发送人用户 ID |
| senderName | string | 发送人登录名 |
| sentAt | datetime | 实际发送时间 |
| createTime | datetime | 记录创建时间 |

---

### 2.7 查询启用的邮件模板列表

```
GET /api/mail/template/list
```

**无请求参数**

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "templateCode": "DISCLOSURE_CONTACT",
      "templateName": "交底联系函",
      "subject": "[交底通知] ${companyName} 知识产权交底",
      "content": "<h3>${companyName}</h3><p>联系人：${contactPerson}</p><p>日期：${disclosureDate}</p>",
      "defaultAttachTypes": "DISCLOSURE_DOC",
      "enabled": 1,
      "createTime": "2026-07-21T17:00:00",
      "updateTime": "2026-07-21T17:00:00"
    }
  ]
}
```

**data 数组元素 (MailTemplate)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 模板主键 |
| templateCode | string | 模板编码，唯一标识，如 `DISCLOSURE_CONTACT` |
| templateName | string | 模板名称 |
| subject | string | 主题模板，支持 `${...}` Thymeleaf 占位符 |
| content | string | 正文模板，HTML 格式，支持 `${...}` 占位符及 Thymeleaf 语法 |
| defaultAttachTypes | string | 默认附带附件类型，逗号分隔 |
| enabled | int | 启用状态：0 禁用 / 1 启用 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

### 2.8 按编码查询模板详情

```
GET /api/mail/template/{templateCode}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateCode | string | 是 | 路径参数，模板编码 |

**成功响应** — 同 2.7 中的单个对象

**失败响应**

```json
{
  "code": 500,
  "message": "模板不存在",
  "data": null
}
```

---

### 2.9 分页查询邮件发送记录

```
GET /api/mail/log/page?pageNum=1&pageSize=10
```

> 仅查询当前登录用户的发送记录，每条记录附带附件数量

**请求参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "sendLog": {
          "id": 1,
          "fromEmail": "sender@qq.com",
          "toEmails": "receiver@example.com",
          "ccEmails": null,
          "subject": "邮件主题",
          "sendStatus": "SUCCESS",
          "errorMessage": null,
          "senderUserId": 1,
          "senderName": "zhangsan",
          "sentAt": "2026-07-23T17:00:00",
          "createTime": "2026-07-23T17:00:00",
          "templateId": null,
          "templateCode": null,
          "content": "<p>邮件正文</p>"
        },
        "attachmentCount": 2
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10
  }
}
```

**data 字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| records | array | 每条元素包含 `sendLog`（MailSendLog）和 `attachmentCount`（附件数量） |
| total | long | 总记录数 |
| current | long | 当前页码 |
| size | long | 每页条数 |

> 附件保存时机：创建发送记录时立即保存附件，无论邮件最终发送成功或失败，附件记录都会保留。

---

### 2.10 查询发送记录详情（含附件）

```
GET /api/mail/log/{id}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 路径参数，发送记录 ID |

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sendLog": {
      "id": 1,
      "fromEmail": "sender@qq.com",
      "toEmails": "receiver@example.com",
      "ccEmails": null,
      "subject": "邮件主题",
      "content": "<p>邮件正文</p>",
      "sendStatus": "SUCCESS",
      "errorMessage": null,
      "senderUserId": 1,
      "senderName": "zhangsan",
      "templateId": null,
      "templateCode": null,
      "sentAt": "2026-07-23T17:00:00",
      "createTime": "2026-07-23T17:00:00"
    },
    "attachments": [
      {
        "id": 10,
        "mailSendLogId": 1,
        "disclosureAttachmentId": null,
        "fileName": "合同.pdf",
        "filePath": "D:\\project\\uploads\\a1b2c3d4.pdf",
        "fileUrl": "/files/a1b2c3d4.pdf?name=合同.pdf",
        "fileSize": 102400,
        "createTime": "2026-07-23T17:00:00"
      }
    ]
  }
}
```

**data.sendLog** — MailSendLog，字段同 2.6 data

**data.attachments[] — MailSendAttachment**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 附件记录主键 |
| mailSendLogId | long | 关联的发送记录 ID |
| disclosureAttachmentId | long/null | 来源交底附件 ID，用户上传则为 null |
| fileName | string | 文件原始名称 |
| filePath | string | 服务器磁盘绝对路径 |
| fileUrl | string | 文件访问 URL（可直接用于下载） |
| fileSize | long | 文件大小，单位字节 |
| createTime | datetime | 创建时间 |

---

## 三、权限体系说明

### 3.1 数据模型

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu
 (用户)       (用户角色关联)      (角色)       (角色菜单关联)      (菜单/权限)
```

- 一个用户可拥有多个角色
- 一个角色可拥有多个菜单（权限）
- 权限标识存储在 `sys_menu.perms` 字段，如 `system:user:list`

### 3.2 接口权限控制

使用 `@RequirePermission` 注解声明接口所需权限：

```java
// 需要单个权限
@RequirePermission("system:user:delete")
@DeleteMapping("/{id}")
public Result deleteUser(@PathVariable Long id) { ... }

// 需要同时满足多个权限（AND）
@RequirePermission(value = {"system:user:add", "system:user:edit"}, logical = Logical.AND)

// 满足任一权限即可（OR）
@RequirePermission(value = {"system:user:view", "system:role:view"}, logical = Logical.OR)

// 类级别注解，类下所有方法生效
@RequirePermission("system:user")
@RestController
@RequestMapping("api/user")
public class UserController { ... }
```

### 3.3 鉴权流程

```
请求 → JwtAuthenticationFilter（解析 Token，加载用户+角色+权限）
     → SecurityFilterChain（路径匹配）
     → PermissionAspect（@RequirePermission 注解校验）
     → Controller
```

---

## 四、错误码参考

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未登录（Token 缺失或无效） |
| 403 | 权限不足 |
| 500 | 系统内部错误 / 业务异常 |
| 600 | 请求参数错误 / 校验失败 |
| 601 | 信息已存在 |
| 901 | 登录超时 |

---

## 五、环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USER` | root | 数据库用户名 |
| `DB_PASSWORD` | 123456 | 数据库密码 |
| `REDIS_PASSWORD` | (空) | Redis 密码 |
| `JWT_SECRET` | intellectual-jwt-secret-key-2026... | JWT 签名密钥（生产环境务必修改） |
