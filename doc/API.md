# 知识产权管理系统 API 文档

## 基础信息

- **Base URL**: `http://localhost:5050`
- **认证方式**: JWT Bearer Token（登录后获取，除公开接口外均需在 Header 中携带）
- **Token 有效期**: 24 小时

### 请求头

```
Authorization: Bearer <token>
Content-Type: application/json
```

### 统一响应格式 `Result<T>`

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 成功，401 未登录，403 权限不足，500 服务端错误，600 参数错误，901 登录超时 |
| message | string | 提示信息 |
| data | any | 响应数据，可能为 null、对象、数组或字符串 |

---

## 一、账户接口 `/api/acount`

### 1.1 获取验证码

```
GET /api/acount/checkCode
```

> **公开接口**，无需认证

**请求参数** (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldCheckCodeKey | string | 否 | 旧的验证码 Key，传入后会先清除旧验证码 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "checkCode": "data:image/png;base64,...",
    "checkCodeKey": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

| 字段 | 说明 |
|------|------|
| checkCode | Base64 格式的算术验证码图片，前端直接放入 `<img src="">` |
| checkCodeKey | 验证码唯一标识，登录/注册时需回传 |

---

### 1.2 用户注册

```
POST /api/acount/register
```

> **公开接口**

**请求体** (JSON)

```json
{
  "loginName": "zhangsan",
  "email": "zhangsan@example.com",
  "phoneNumber": "13800138000",
  "password": "123456",
  "checkCodeKey": "uuid-from-checkCode",
  "checkCode": "8"
}
```

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| loginName | string | 是 | 3~30 位 |
| email | string | 是 | 合法邮箱格式 |
| phoneNumber | string | 是 | `1[3-9]` 开头 11 位 |
| password | string | 是 | 6~20 位 |
| checkCodeKey | string | 否 | 验证码 Key |
| checkCode | string | 是 | 用户输入的验证码结果 |

**响应** — 成功

```json
{ "code": 200, "message": "success", "data": "注册成功" }
```

**响应** — 失败

```json
{ "code": 500, "message": "账号已存在", "data": null }
```

> 可能失败原因：账号已存在、邮箱已被注册、验证码错误或过期

---

### 1.3 用户登录

```
POST /api/acount/login
```

> **公开接口**

**请求体** (JSON)

```json
{
  "loginName": "zhangsan",
  "password": "123456",
  "checkCodeKey": "uuid-from-checkCode",
  "checkCode": "8"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginName | string | 否 | 登录账号（与 phoneNumber 二选一） |
| phoneNumber | string | 否 | 手机号（与 loginName 二选一） |
| password | string | 是 | 密码 |
| checkCodeKey | string | 否 | 验证码 Key |
| checkCode | string | 是 | 验证码结果 |

**响应** — 成功

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "loginName": "zhangsan",
    "userName": "张三",
    "roles": ["admin"],
    "permissions": ["system:user:list", "patent:disclosure:list"],
    "email": "zhangsan@example.com"
  }
}
```

> 前端需将 `token` 存入 localStorage 并在后续请求 Header 中携带

---

### 1.4 退出登录

```
POST /api/acount/logout
```

> **需认证**

**请求体** — 无

**响应**

```json
{ "code": 200, "message": "退出成功", "data": null }
```

---

### 1.5 获取当前用户信息

```
GET /api/acount/me
```

> **需认证**

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "loginName": "zhangsan",
    "roles": ["admin"],
    "email": "123456@qq.com",
    "permissions": ["system:user:list", "patent:disclosure:list"]
  }
}
```

> 未登录返回 `code: 401, message: "未登录"`

---

### 1.6 保存邮箱授权码

```
POST /api/acount/authCode
```

> **需认证**

**请求参数** (Form / Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户 ID |
| email | string | 是 | 邮箱地址 |
| authCode | string | 是 | 邮箱 SMTP 授权码 |

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

---

### 1.7 修改密码

```
POST /api/acount/password
```

> **需认证**

**请求参数** (Form / Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户 ID |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码 |

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

---

## 二、用户接口 `/api/user`

### 2.1 用户列表

```
GET /api/user/list
```

> 需权限：`system:user:list`

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "userId": 1,
      "deptId": null,
      "loginName": "zhangsan",
      "userName": "张三",
      "userType": "01",
      "email": "zhangsan@example.com",
      "phoneNumber": "13800138000",
      "sex": "0",
      "avatar": null,
      "status": "0",
      "delFlag": "0",
      "loginIp": null,
      "loginDate": null,
      "createTime": "2026-07-21T10:00:00",
      "updateTime": "2026-07-21T10:00:00"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 主键 |
| loginName | string | 登录账号 |
| userName | string | 用户昵称 |
| userType | string | 00 系统用户，01 注册用户 |
| email | string | 邮箱 |
| phoneNumber | string | 手机号 |
| sex | string | 0 男，1 女，2 未知 |
| status | string | 0 正常，1 停用 |
| delFlag | string | 0 正常，2 已删除 |

---

## 三、用户角色接口 `/sys-user-role`

> 对应数据表：`sys_user_role`（用户-角色关联表，无独立详情/修改接口）

### 3.1 分页列表

```
GET /sys-user-role/list
```

> 需权限：`system:userRole:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| userId | long | 否 | — | 用户 ID（精确） |
| roleId | long | 否 | — | 角色 ID（精确） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `UserRole[]`

### 3.2 全部列表（不分页）

```
GET /sys-user-role/all
```

> 需权限：`system:userRole:list`

### 3.3 新增

```
POST /sys-user-role
```

> 需权限：`system:userRole:add`

**请求体** (JSON) — `UserRole` 对象（含 `userId`、`roleId`）

### 3.4 删除

```
DELETE /sys-user-role/{roleId}
```

> 需权限：`system:userRole:delete`

### 3.5 批量删除

```
DELETE /sys-user-role/batch
```

> 需权限：`system:userRole:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 四、角色接口 `/sys-role`

> 对应数据表：`sys_role`

### 4.1 分页列表

```
GET /sys-role/list
```

> 需权限：`system:role:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| roleName | string | 否 | — | 角色名称（模糊） |
| roleKey | string | 否 | — | 权限字符（模糊） |
| status | string | 否 | — | 状态（精确，0 正常 1 停用） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `Role[]`

**Role 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | long | 角色 ID |
| roleName | string | 角色名称 |
| roleKey | string | 角色权限字符串 |
| roleSort | int | 显示顺序 |
| dataScope | string | 数据范围（1 全部 2 自定义 3 本部门 4 本部门及以下） |
| status | string | 状态（0 正常 1 停用） |
| delFlag | string | 删除标志（0 正常 2 已删除） |
| createBy | string | 创建者 |
| createTime | datetime | 创建时间 |
| updateBy | string | 更新者 |
| updateTime | datetime | 更新时间 |
| remark | string | 备注 |

### 4.2 全部列表（不分页）

```
GET /sys-role/all
```

> 需权限：`system:role:list`

### 4.3 详情

```
GET /sys-role/{roleId}
```

> 需权限：`system:role:query`

### 4.4 新增

```
POST /sys-role
```

> 需权限：`system:role:add`

**请求体** (JSON) — `Role` 对象

### 4.5 修改

```
PUT /sys-role
```

> 需权限：`system:role:edit`

**请求体** (JSON) — `Role` 对象，`roleId` 必填

### 4.6 删除

```
DELETE /sys-role/{roleId}
```

> 需权限：`system:role:delete`

### 4.7 批量删除

```
DELETE /sys-role/batch
```

> 需权限：`system:role:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 五、角色菜单接口 `/sys-role-menu`

> 对应数据表：`sys_role_menu`（角色-菜单关联表，无独立详情/修改接口）

### 5.1 分页列表

```
GET /sys-role-menu/list
```

> 需权限：`system:roleMenu:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| roleId | long | 否 | — | 角色 ID（精确） |
| menuId | long | 否 | — | 菜单 ID（精确） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `RoleMenu[]`

### 5.2 全部列表（不分页）

```
GET /sys-role-menu/all
```

> 需权限：`system:roleMenu:list`

### 5.3 新增

```
POST /sys-role-menu
```

> 需权限：`system:roleMenu:add`

**请求体** (JSON) — `RoleMenu` 对象（含 `roleId`、`menuId`）

### 5.4 删除

```
DELETE /sys-role-menu/{menuId}
```

> 需权限：`system:roleMenu:delete`

### 5.5 批量删除

```
DELETE /sys-role-menu/batch
```

> 需权限：`system:roleMenu:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 六、菜单接口 `/sys-menu`

> 对应数据表：`sys_menu`

### 6.1 分页列表

```
GET /sys-menu/list
```

> 需权限：`system:menu:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| menuName | string | 否 | — | 菜单名称（模糊） |
| menuType | string | 否 | — | 菜单类型（M 目录 C 菜单 F 按钮） |
| visible | string | 否 | — | 状态（0 显示 1 隐藏） |
| parentId | long | 否 | — | 父菜单 ID（精确） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `Menu[]`

**Menu 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| menuId | long | 菜单 ID |
| menuName | string | 菜单名称 |
| parentId | long | 父菜单 ID |
| orderNum | int | 显示顺序 |
| url | string | 请求地址 |
| target | string | 打开方式（menuItem 页签 / menuBlank 新窗口） |
| menuType | string | 菜单类型（M 目录 C 菜单 F 按钮） |
| visible | string | 菜单状态（0 显示 1 隐藏） |
| isRefresh | string | 是否刷新（0 刷新 1 不刷新） |
| perms | string | 权限标识 |
| icon | string | 菜单图标 |
| createBy | string | 创建者 |
| createTime | datetime | 创建时间 |
| updateBy | string | 更新者 |
| updateTime | datetime | 更新时间 |
| remark | string | 备注 |

### 6.2 全部列表（不分页）

```
GET /sys-menu/all
```

> 需权限：`system:menu:list`

### 6.3 详情

```
GET /sys-menu/{menuId}
```

> 需权限：`system:menu:query`

### 6.4 新增

```
POST /sys-menu
```

> 需权限：`system:menu:add`

**请求体** (JSON) — `Menu` 对象

### 6.5 修改

```
PUT /sys-menu
```

> 需权限：`system:menu:edit`

**请求体** (JSON) — `Menu` 对象，`menuId` 必填

### 6.6 删除

```
DELETE /sys-menu/{menuId}
```

> 需权限：`system:menu:delete`

### 6.7 批量删除

```
DELETE /sys-menu/batch
```

> 需权限：`system:menu:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 七、邮件模板接口 `/api/mail-template`

> 对应数据表：`mail_template`

### 7.1 分页列表

```
GET /api/mail-template/list
```

> 需权限：`system:mailTemplate:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| templateCode | string | 否 | — | 模板编码（模糊） |
| templateName | string | 否 | — | 模板名称（模糊） |
| enabled | int | 否 | — | 启用状态（0 禁用 1 启用） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `MailTemplate[]`

**MailTemplate 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| templateCode | string | 模板编码 |
| templateName | string | 模板名称 |
| subject | string | 主题模板（支持占位符） |
| content | string | 正文模板（支持占位符，Thymeleaf 渲染） |
| defaultAttachTypes | string | 默认附带附件类型，逗号分隔 |
| enabled | int | 是否启用（0 否 1 是） |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 7.2 全部列表（不分页）

```
GET /api/mail-template/all
```

> 需权限：`system:mailTemplate:list`

### 7.3 详情

```
GET /api/mail-template/{id}
```

> 需权限：`system:mailTemplate:query`

### 7.4 新增

```
POST /api/mail-template
```

> 需权限：`system:mailTemplate:add`

**请求体** (JSON) — `MailTemplate` 对象

### 7.5 修改

```
PUT /api/mail-template
```

> 需权限：`system:mailTemplate:edit`

**请求体** (JSON) — `MailTemplate` 对象，`id` 必填

### 7.6 删除

```
DELETE /api/mail-template/{id}
```

> 需权限：`system:mailTemplate:delete`

### 7.7 批量删除

```
DELETE /api/mail-template/batch
```

> 需权限：`system:mailTemplate:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 八、文件接口

### 8.1 上传文件

```
POST /upload
```

> **需认证**
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 文件 |

**响应**

```json
{
  "code": 200,
  "data": "/files/a1b2c3d4.pdf?name=原始文件名.pdf"
}
```

> 返回值为可直接访问的文件 URL

---

### 8.2 查看/下载文件

```
GET /files/{fileId}
```

> **需认证**

**路径参数**

| 参数 | 说明 |
|------|------|
| fileId | 上传后返回的文件 ID（含扩展名，如 `a1b2c3d4.pdf`） |

**请求参数** (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 原始文件名，用于设置下载响应头 |

**响应** — 文件二进制流，浏览器根据 Content-Type 决定预览或下载

---

## 九、邮件接口 `/api/mail`

### 9.1 发送邮件

```
POST /api/mail/sendMaill
```

> **需认证**
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| to | string | 是 | — | 收件人邮箱 |
| subject | string | 是 | — | 邮件主题 |
| content | string | 是 | — | 邮件正文 |
| cc | string | 否 | — | 抄送邮箱 |
| isHtml | boolean | 否 | false | 正文是否为 HTML |
| files | file | 否 | — | 附件 |

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

---

### 9.2 模板邮件发送

```
POST /api/mail/sendMailWithTemplate
```

> **需认证**

**请求体** (JSON)

```json
{
  "to": "user@example.com",
  "cc": "cc@example.com",
  "subject": "邮件主题",
  "text": "正文（不使用模板时）",
  "templateCode": "WELCOME",
  "templateData": {
    "userName": "张三",
    "projectName": "某某专利"
  },
  "attachmentUrls": [
    "/files/a1b2c3d4.pdf?name=附件1.pdf"
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| to | string | 是 | 收件人，逗号/分号分隔多人 |
| cc | string | 否 | 抄送，逗号/分号分隔多人 |
| subject | string | 否 | 主题（模板优先时可为空） |
| text | string | 否 | 正文（模板优先时可为空） |
| templateCode | string | 否 | 模板编码，选用模板时传入 |
| templateData | map | 否 | 模板变量，用于 Thymeleaf 渲染 |
| attachmentUrls | string[] | 否 | 附件 URL 列表，来自上传接口返回的路径 |

---

## 十、T表 — 专利交底 `/api/ttable`

> 对应数据表：`patent_disclosure`

### 10.1 分页列表

```
GET /api/ttable/list
```

> 需权限：`patent:disclosure:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| disclosureName | string | 否 | — | 交底名称（模糊匹配） |
| patentType | string | 否 | — | 专利类型（精确匹配） |
| patentStatus | string | 否 | — | 专利状态（精确匹配） |
| internalNo | string | 否 | — | 内部编号（精确匹配） |
| applicant | string | 否 | — | 申请人（模糊匹配） |

**响应**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "tempNo": "T250101",
        "internalNo": "P2025101",
        "patentStatus": "受理",
        "requirement": "一周内提交",
        "disclosureName": "一种新型散热装置",
        "applicant": "某某公司",
        "inventor": "张三、李四",
        "patentType": "发明",
        "invitedToGroup": 1,
        "contactPerson": "张三",
        "manager": "王五",
        "agent": "某某代理所",
        "sponsor": "赵六",
        "sponsorUserId": 5,
        "disclosureDate": "2026-07-01",
        "disclosureDays": 30,
        "remark": "备注内容",
        "contactInfo": "QQ:123456",
        "contactEmail": "zhangsan@example.com",
        "contactPhone": "13800138000",
        "entryUserId": 1,
        "entryUserName": "admin",
        "copyFromId": null,
        "noGenerateMode": "AUTO",
        "finalizedAt": null,
        "pendingReportAt": null,
        "syncedToPatent": 0,
        "patentApplicationId": null,
        "createTime": "2026-07-23T16:00:00",
        "updateTime": "2026-07-23T16:00:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 10.2 高级搜索

```
POST /api/ttable/search?pageNum=1&pageSize=10
```

> 需权限：`patent:disclosure:list`

**请求体** (JSON) — `PatentDisclosure` 对象，所有字段均为可选，传入非 null 字段参与筛选：

| 字段 | 匹配方式 | 说明 |
|------|----------|------|
| disclosureName | 模糊 | 交底名称 |
| patentType | 精确 | 专利类型 |
| patentStatus | 精确 | 专利状态 |
| internalNo | 精确 | 内部编号 |
| tempNo | 精确 | 临时编号 |
| applicant | 模糊 | 申请人 |
| inventor | 模糊 | 发明人 |
| agent | 模糊 | 代理人 |
| sponsor | 模糊 | 主办人 |
| sponsorUserId | 精确 | 主办人用户ID |
| contactPerson | 模糊 | 联系人 |
| syncedToPatent | 精确 | 0 未同步 1 已同步 |

### 10.3 全部列表（不分页）

```
GET /api/ttable/all
```

> 需权限：`patent:disclosure:list`

**响应** — `data` 为 `PatentDisclosure[]` 数组

### 10.4 详情

```
GET /api/ttable/{id}
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `PatentDisclosure` 对象；不存在返回 `code: 500, message: "交底记录不存在"`

### 10.5 详情（含关联数据）

```
GET /api/ttable/{id}/detail
```

> 需权限：`patent:disclosure:query`

**响应**

```json
{
  "code": 200,
  "data": {
    "disclosure": { "id": 1, "disclosureName": "..." },
    "attachments": [ { "id": 1, "fileName": "交底书.docx", "bizType": "DISCLOSURE_DOC" } ],
    "statusLogs": [ { "id": 1, "fromStatus": "草稿", "toStatus": "定稿", "operatorName": "张三" } ],
    "fees": [ { "id": 1, "feeType": "官费", "feeAmount": 500.00, "paymentStatus": "PENDING" } ],
    "invoices": [ { "id": 1, "invoiceType": "普票", "invoiceAmount": 500.00, "invoiceStatus": "ISSUED" } ],
    "packages": [ { "id": 1, "packageType": "XML_PACKAGE", "fileName": "申请包.xml", "confirmStatus": "UNCONFIRMED" } ]
  }
}
```

### 10.6 新增

```
POST /api/ttable
```

> 需权限：`patent:disclosure:add`

**请求体** (JSON) — `PatentDisclosure` 对象

### 10.7 修改

```
PUT /api/ttable
```

> 需权限：`patent:disclosure:edit`

**请求体** (JSON) — `PatentDisclosure` 对象，`id` 字段必填，否则返回 `"ID不能为空"`

### 10.8 删除

```
DELETE /api/ttable/{id}
```

> 需权限：`patent:disclosure:delete`

### 10.9 批量删除

```
DELETE /api/ttable/batch
```

> 需权限：`patent:disclosure:delete`

**请求体** (JSON)

```json
[1, 2, 3]
```

### 10.10 交底附件列表

```
GET /api/ttable/{id}/attachments
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `DisclosureAttachment[]` 数组（仅返回 `deleted=0` 的未删除附件，按 `sortNo` 升序）

**DisclosureAttachment 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 交底ID |
| internalNo | string | 内部编号 |
| bizType | string | DISCLOSURE_DOC 交底书 / DISCLOSURE_OTHER 其他 / MAIL_EXTRA 邮件附带 |
| fileName | string | 原始文件名 |
| fileExt | string | 扩展名 |
| filePath | string | 存储路径 |
| fileUrl | string | 访问URL |
| fileSize | long | 字节数 |
| contentType | string | MIME类型 |
| isRequired | int | 是否必填（1 是） |
| sortNo | int | 排序 |
| uploadUserId | long | 上传人ID |
| uploadUserName | string | 上传人姓名 |
| deleted | int | 逻辑删除（0 否 1 是） |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 10.11 交底状态变更日志

```
GET /api/ttable/{id}/status-logs
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `DisclosureStatusLog[]` 数组（按 `createTime` 降序）

**DisclosureStatusLog 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 交底ID |
| fromStatus | string | 原状态 |
| toStatus | string | 新状态 |
| operatorUserId | long | 操作人ID |
| operatorName | string | 操作人姓名 |
| remark | string | 备注/原因 |
| createTime | datetime | 创建时间 |

### 10.12 交底关联费用

```
GET /api/ttable/{id}/fees
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `FeePayment[]` 数组（按 `createTime` 降序）

**FeePayment 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 交底ID |
| internalNo | string | 内部编号 |
| tempNo | string | 临时编号 |
| disclosureName | string | 交底/专利名称 |
| applicant | string | 申请人/缴费主体 |
| feeType | string | 费用类型（官费/代理费等） |
| feeAmount | decimal | 金额 |
| paymentDeadline | date | 缴费止期 |
| paymentDate | date | 实缴日期 |
| paymentStatus | string | PENDING 待缴 / PAID 已缴 / PARTIAL 部分 / VOID 作废 |
| payer | string | 付款方 |
| remark | string | 备注 |
| source | string | 来源 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 10.13 交底关联开票

```
GET /api/ttable/{id}/invoices
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `Invoice[]` 数组（按 `createTime` 降序）

**Invoice 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 交底ID |
| internalNo | string | 内部编号 |
| tempNo | string | 临时编号 |
| disclosureName | string | 交底/专利名称 |
| applicant | string | 申请人 |
| invoiceTitle | string | 发票抬头 |
| taxNo | string | 税号 |
| invoiceType | string | 发票类型（普票/专票等） |
| invoiceAmount | decimal | 开票金额 |
| invoiceStatus | string | PENDING 待开 / ISSUED 已开 / VOID 作废 |
| invoiceNo | string | 发票号码 |
| invoiceDate | date | 开票日期 |
| remark | string | 备注 |
| source | string | 来源 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 10.14 复制交底

```
POST /api/ttable/copy
```

> 需权限：`patent:disclosure:add`

**请求体** (JSON)

```json
{ "sourceId": 1 }
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceId | long | 是 | 源交底ID |

**响应** — `data` 为新创建的 `PatentDisclosure` 对象。新交底的 `copyFromId` 会被设为 `sourceId`，`tempNo`/`internalNo`/`patentStatus`/`syncedToPatent` 等字段会重置。

### 10.15 按主办人查询

```
GET /api/ttable/by-sponsor/{sponsorUserId}
```

> 需权限：`patent:disclosure:list`

**响应** — `data` 为 `PatentDisclosure[]` 数组（按 `createTime` 降序）

### 10.16 变更状态

```
POST /api/ttable/{id}/status
```

> 需权限：`patent:disclosure:edit`

**请求体** (JSON)

```json
{
  "toStatus": "定稿",
  "operatorUserId": 1,
  "operatorName": "张三",
  "remark": "已定稿，进入下一流程"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| toStatus | string | 是 | 新状态 |
| operatorUserId | long | 否 | 操作人ID |
| operatorName | string | 否 | 操作人姓名 |
| remark | string | 否 | 备注/原因 |

> 系统自动记录旧状态 → 新状态的变更日志到 `disclosure_status_log` 表。

### 10.17 上传附件

```
POST /api/ttable/{id}/attachments
```

> 需权限：`patent:disclosure:add`
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| file | file | 是 | — | 附件文件 |
| bizType | string | 否 | DISCLOSURE_OTHER | DISCLOSURE_DOC 交底书 / DISCLOSURE_OTHER 其他 |
| uploadUserId | long | 否 | — | 上传人ID |
| uploadUserName | string | 否 | — | 上传人姓名 |

**响应** — `data` 为新创建的 `DisclosureAttachment` 对象，包含文件访问 URL。

### 10.18 删除附件（逻辑删除）

```
DELETE /api/ttable/attachments/{attachmentId}
```

> 需权限：`patent:disclosure:delete`

> 将 `deleted` 字段置为 1，不会物理删除文件。

### 10.19 交底申请包列表

```
GET /api/ttable/{id}/packages
```

> 需权限：`patent:disclosure:query`

**响应** — `data` 为 `ApplicationPackage[]` 数组（按 `createTime` 降序）

### 10.20 上传申请包

```
POST /api/ttable/{id}/packages
```

> 需权限：`patent:disclosure:add`
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 申请包文件（XML或WORD） |
| packageType | string | 是 | XML_PACKAGE / FIVE_BOOKS_WORD |
| uploadUserId | long | 否 | 上传人ID |
| uploadUserName | string | 否 | 上传人姓名 |

> 新增时 `confirmStatus` 默认为 `UNCONFIRMED`，`versionNo` 为 1，`isCurrent` 为 1。

**响应** — `data` 为新创建的 `ApplicationPackage` 对象，包含文件访问 URL。

---

## 十一、P表 — 专利业务 `/api/ptable`

> P表包含 5 个子模块，每个模块结构与 T表完全一致：`list` / `all` / `{id}` / POST / PUT / `DELETE {id}` / `DELETE batch`
> 为简明起见，以下只列出各模块的 URL 前缀、权限前缀、实体字段及 list 筛选参数。"新增/修改" 请求体均为各实体对象的 JSON。

---

### 11.1 新申请 `patent_new_application`

| 项目 | 值 |
|------|-----|
| URL 前缀 | `/api/ptable/new-application` |
| 权限前缀 | `patent:newApplication` |

**分页筛选参数** (list)

| 参数 | 类型 | 匹配方式 |
|------|------|----------|
| patentName | string | 模糊 |
| applicationNo | string | 精确 |
| patentType | string | 精确 |
| applicant | string | 模糊 |

**实体字段** — `PatentNewApplication`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| internalNo | string | 内部编号 |
| patentName | string | 发明创造名称 |
| applicationNo | string | 申请号/专利号 |
| applicant | string | 申请人 |
| inventor | string | 发明人 |
| sponsor | string | 主办人 |
| agent | string | 委托书代理人 |
| applicationDate | date | 申请日 |
| notification | string | 通知书 |
| issueDate | date | 发文日 |
| preExamMark | string | 非正标-预审标 |
| paymentDeadline | date | 缴费止期 |
| feeAmount | decimal | 费用金额 |
| paymentDate | string | 缴费时间 |
| seqNo | int | 序号 |
| patentType | string | 类型（发明/实用新型/外观） |
| dasCode | string | DAS码 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

### 11.2 补漏 `patent_supplementary`

| 项目 | 值 |
|------|-----|
| URL 前缀 | `/api/ptable/supplementary` |
| 权限前缀 | `patent:supplementary` |

**分页筛选参数** (list)

| 参数 | 类型 | 匹配方式 |
|------|------|----------|
| patentName | string | 模糊 |
| applicationNo | string | 精确 |
| applicant | string | 模糊 |

**实体字段** — `PatentSupplementary`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| seqNo | int | 序号 |
| applicationNo | string | 申请号/专利号 |
| patentName | string | 发明创造名称 |
| applicant | string | 申请人 |
| inventor | string | 发明人 |
| sponsor | string | 主办人 |
| agent | string | 委托书代理人 |
| applicationDate | date | 申请日 |
| notification | string | 通知书 |
| issueDate | date | 发文日 |
| feeReduction | string | 费减 |
| remark | string | 备注 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

### 11.3 PCT `patent_pct`

| 项目 | 值 |
|------|-----|
| URL 前缀 | `/api/ptable/pct` |
| 权限前缀 | `patent:pct` |

**分页筛选参数** (list)

| 参数 | 类型 | 匹配方式 |
|------|------|----------|
| applicationName | string | 模糊 |
| applicationNo | string | 精确 |
| status | string | 精确 |
| applicant | string | 模糊 |

**实体字段** — `PatentPct`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| seqNo | int | 序号 |
| pctInternalNo | string | PCT 内部编号 |
| status | string | 状态 |
| issueDate | date | 发文日 |
| priorInternalNo | string | 在先内部编号 |
| priorApplicationNo | string | 在先申请号 |
| priorApplicationDate | date | 在先申请日 |
| pctApplicationDate | date | PCT 申请日 |
| applicationName | string | 申请名称 |
| applicationNo | string | 申请号（PCT 号） |
| applicant | string | 申请人 |
| inventor | string | 发明人 |
| sponsor | string | 主办人 |
| agent | string | 委托书代理人 |
| preliminaryConclusion | string | 初检结论 |
| remark | string | 备注 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

### 11.4 中间著变 `patent_intermediate_change`

| 项目 | 值 |
|------|-----|
| URL 前缀 | `/api/ptable/intermediate-change` |
| 权限前缀 | `patent:intermediateChange` |

**分页筛选参数** (list)

| 参数 | 类型 | 匹配方式 |
|------|------|----------|
| patentName | string | 模糊 |
| applicationNo | string | 精确 |
| businessType | string | 精确 |
| status | string | 精确 |
| applicant | string | 模糊 |

**实体字段** — `PatentIntermediateChange`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| seqNo | int | 序号 |
| internalNo | string | 内部编号 |
| businessType | string | 业务类型（转让/转我所/著录变更） |
| applicationNo | string | 申请号 |
| patentName | string | 发明创造名称 |
| applicant | string | 申请人 |
| inventor | string | 发明人 |
| sponsor | string | 主办人 |
| agent | string | 委托书代理人 |
| applicationDate | date | 申请日 |
| status | string | 状态 |
| issueDate | date | 发文日 |
| feeReductionInfo | string | 非正标-费减情况 |
| submitDate | date | 提交日期 |
| paymentDeadline | date | 缴费止期 |
| feeAmount | decimal | 费用金额 |
| paymentStatus | string | 缴费状态 |
| remark1 | string | 备注1 |
| remark2 | string | 备注2 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

### 11.5 复审无效 `patent_reexamination`

| 项目 | 值 |
|------|-----|
| URL 前缀 | `/api/ptable/reexamination` |
| 权限前缀 | `patent:reexamination` |

**分页筛选参数** (list)

| 参数 | 类型 | 匹配方式 |
|------|------|----------|
| patentName | string | 模糊 |
| applicationNo | string | 精确 |
| patentType | string | 精确 |
| category | string | 精确 |
| status | string | 精确 |

**实体字段** — `PatentReexamination`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| seqNo | int | 序号 |
| patentType | string | 类型（发明/实用新型/外观） |
| category | string | 分类（复审/无效） |
| caseNo | string | 案件编号 |
| internalNo | string | 内部编号 |
| applicationNo | string | 申请号 |
| patentName | string | 专利名称 |
| applicant | string | 申请人 |
| sponsor | string | 主办人 |
| agent | string | 委托书代理人 |
| notification | string | 通知书 |
| issueDate | date | 发文日 |
| submitDate | date | 提交日期 |
| queryInfo | string | 25.6.12 查询 |
| officialFee | decimal | 规费 |
| paymentDate | string | 缴费时间 |
| note1 | string | 附注1 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

---

## 十二、代理人/申请人接口 `/api/agent`

> 对应数据表：`agent`、`applicant`

### 12.1 代理人分页列表

```
GET /api/agent/list
```

> 需权限：`patent:agent:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| name | string | 否 | — | 代理人姓名（模糊匹配） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `Agent[]`

**Agent 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| name | string | 代理人姓名 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 12.2 代理人全部列表（不分页）

```
GET /api/agent/all
```

> 需权限：`patent:agent:list`

### 12.3 代理人详情

```
GET /api/agent/{id}
```

> 需权限：`patent:agent:query`

### 12.4 代理人新增

```
POST /api/agent
```

> 需权限：`patent:agent:add`

**请求体** (JSON) — `Agent` 对象（`name` 必填）

### 12.5 代理人修改

```
PUT /api/agent
```

> 需权限：`patent:agent:edit`

**请求体** (JSON) — `Agent` 对象，`id` 必填

### 12.6 代理人删除

```
DELETE /api/agent/{id}
```

> 需权限：`patent:agent:delete`

### 12.7 代理人批量删除

```
DELETE /api/agent/batch
```

> 需权限：`patent:agent:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

### 12.8 申请人分页列表

```
GET /api/agent/applicant/list
```

> 需权限：`patent:applicant:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| name | string | 否 | — | 申请人姓名（模糊匹配） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `Applicant[]`

**Applicant 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| name | string | 申请人姓名 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 12.9 申请人全部列表（不分页）

```
GET /api/agent/applicant/all
```

> 需权限：`patent:applicant:list`

### 12.10 申请人详情

```
GET /api/agent/applicant/{id}
```

> 需权限：`patent:applicant:query`

### 12.11 申请人新增

```
POST /api/agent/applicant
```

> 需权限：`patent:applicant:add`

**请求体** (JSON) — `Applicant` 对象（`name` 必填）

### 12.12 申请人修改

```
PUT /api/agent/applicant
```

> 需权限：`patent:applicant:edit`

**请求体** (JSON) — `Applicant` 对象，`id` 必填

### 12.13 申请人删除

```
DELETE /api/agent/applicant/{id}
```

> 需权限：`patent:applicant:delete`

### 12.14 申请人批量删除

```
DELETE /api/agent/applicant/batch
```

> 需权限：`patent:applicant:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 十三、申请包接口 `/api/application-package`

> 对应数据表：`application_package`

### 13.1 分页列表

```
GET /api/application-package/list
```

> 需权限：`patent:applicationPackage:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| disclosureId | long | 否 | — | 交底ID（精确） |
| packageType | string | 否 | — | 包类型：XML_PACKAGE / FIVE_BOOKS_WORD（精确） |
| confirmStatus | string | 否 | — | UNCONFIRMED / CONFIRMED / SUBMITTED（精确） |
| internalNo | string | 否 | — | 内部编号（精确） |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 为 `ApplicationPackage[]`

**ApplicationPackage 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 交底ID |
| internalNo | string | 内部编号 |
| packageType | string | XML_PACKAGE / FIVE_BOOKS_WORD |
| fileName | string | 原始文件名 |
| fileExt | string | 扩展名 |
| filePath | string | 存储路径 |
| fileUrl | string | 访问URL |
| fileSize | long | 字节数 |
| contentType | string | MIME类型 |
| versionNo | int | 版本号，覆盖上传+1 |
| isCurrent | int | 是否当前有效版本（0 否 1 是） |
| currentTypeKey | string | 当前版本唯一标识 |
| uploadUserId | long | 上传人ID（主办） |
| uploadUserName | string | 上传人姓名 |
| uploadTime | datetime | 上传时间 |
| confirmStatus | string | UNCONFIRMED 未确认 / CONFIRMED 可提交 / SUBMITTED 已交国知局 |
| confirmUserId | long | 确认人ID（流程） |
| confirmUserName | string | 确认人姓名 |
| confirmTime | datetime | 确认时间 |
| remark | string | 备注 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 13.2 全部列表（不分页）

```
GET /api/application-package/all
```

> 需权限：`patent:applicationPackage:list`

### 13.3 详情

```
GET /api/application-package/{id}
```

> 需权限：`patent:applicationPackage:query`

### 13.4 新增

```
POST /api/application-package
```

> 需权限：`patent:applicationPackage:add`

**请求体** (JSON) — `ApplicationPackage` 对象。未传 `confirmStatus` 默认 `UNCONFIRMED`，未传 `versionNo` 默认 `1`，未传 `isCurrent` 默认 `1`。

### 13.5 修改

```
PUT /api/application-package
```

> 需权限：`patent:applicationPackage:edit`

**请求体** (JSON) — `ApplicationPackage` 对象，`id` 必填

### 13.6 删除

```
DELETE /api/application-package/{id}
```

> 需权限：`patent:applicationPackage:delete`

### 13.7 批量删除

```
DELETE /api/application-package/batch
```

> 需权限：`patent:applicationPackage:delete`

**请求体** (JSON) — `[1, 2, 3]`

### 13.8 按交底ID查询

```
GET /api/application-package/by-disclosure/{disclosureId}
```

> 需权限：`patent:applicationPackage:list`

**响应** — `data` 为 `ApplicationPackage[]` 数组（按 `createTime` 降序）

### 13.9 确认申请包

```
PUT /api/application-package/{id}/confirm
```

> 需权限：`patent:applicationPackage:edit`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| confirmUserId | long | 是 | 确认人用户ID |
| confirmUserName | string | 是 | 确认人姓名 |

**响应**

```json
{ "code": 200, "message": "确认成功", "data": { ... } }
```

> 将 `confirmStatus` 设为 `CONFIRMED`，同时记录确认人和确认时间

---

## 附录 A：权限标识汇总

### 系统管理
```
system:user:list               — 用户列表
system:userRole:list|add|delete       — 用户角色关联
system:role:list|query|add|edit|delete — 角色管理
system:menu:list|query|add|edit|delete — 菜单管理
system:roleMenu:list|add|delete        — 角色菜单关联
system:mailTemplate:list|query|add|edit|delete — 邮件模板管理
```

### T表（专利交底）
```
patent:disclosure:list    — 列表/搜索
patent:disclosure:query   — 详情/附件/日志/费用/开票
patent:disclosure:add     — 新增
patent:disclosure:edit    — 修改
patent:disclosure:delete  — 删除
```

### P表（专利业务）
```
patent:newApplication:list|query|add|edit|delete      — 新申请
patent:supplementary:list|query|add|edit|delete        — 补漏
patent:pct:list|query|add|edit|delete                  — PCT
patent:intermediateChange:list|query|add|edit|delete   — 中间著变
patent:reexamination:list|query|add|edit|delete        — 复审无效
```

### 代理人/申请人
```
patent:agent:list|query|add|edit|delete      — 代理人管理
patent:applicant:list|query|add|edit|delete  — 申请人管理
```

### 申请包
```
patent:applicationPackage:list|query|add|edit|delete  — 申请包管理
```

---

## 附录 B：通用响应码

| code | 含义 |
|------|------|
| 200 | 请求成功 |
| 401 | 未登录 |
| 403 | 权限不足 |
| 404 | 请求地址不存在 |
| 500 | 服务器错误 |
| 600 | 请求参数错误 |
| 601 | 信息已存在 |
| 901 | 登录超时 |
