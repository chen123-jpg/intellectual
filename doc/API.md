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

### 1.2 获取手机验证码

```
GET /api/acount/getSmsCode
```

> **公开接口**，无需认证

**请求参数** (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mobile | string | 是 | 手机号码（`1[3-9]` 开头 11 位） |

**响应**

```json
{ "code": 200, "message": "验证码发送成功", "data": null }
```

> 验证码 5 分钟内有效，同一手机号 60 秒内不允许重复发送。验证码存入 Redis，登录/注册时校验后立即删除。

---

### 1.3 用户注册

```
POST /api/acount/register
```

> **公开接口**。注册时系统会根据邮箱域名自动识别 SMTP 服务器地址与端口，无需手动填写。

**请求体** (JSON)

```json
{
  "loginName": "zhangsan",
  "email": "zhangsan@example.com",
  "phoneNumber": "13800138000",
  "smsCode": "123456",
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
| smsCode | string | 是 | 6 位手机验证码 |
| password | string | 是 | 6~20 位 |
| checkCodeKey | string | 否 | 图形验证码 Key |
| checkCode | string | 是 | 用户输入的图形验证码结果 |

**响应** — 成功

```json
{ "code": 200, "message": "success", "data": "注册成功" }
```

**响应** — 失败

```json
{ "code": 500, "message": "账号已存在", "data": null }
```

> 可能失败原因：账号已存在、邮箱已被注册、图形验证码错误或过期、手机验证码错误或过期

---

### 1.4 用户登录

```
POST /api/acount/login
```

> **公开接口**。支持两种登录方式：**账号+密码** 或 **手机号+密码**，均需提供手机验证码和图形验证码。

**请求体** (JSON)

```json
{
  "loginName": "zhangsan",
  "phoneNumber": "13800138000",
  "smsCode": "123456",
  "password": "123456",
  "checkCodeKey": "uuid-from-checkCode",
  "checkCode": "8"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginName | string | 否 | 登录账号（与 phoneNumber 二选一，优先使用 loginName） |
| phoneNumber | string | 否 | 手机号（loginName 为空时使用） |
| smsCode | string | 是 | 6 位手机验证码 |
| password | string | 是 | 密码 |
| checkCodeKey | string | 否 | 图形验证码 Key |
| checkCode | string | 是 | 图形验证码结果 |

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

### 1.5 退出登录

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

### 1.6 获取当前用户信息

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

### 1.7 保存邮箱授权码

```
POST /api/acount/authCode
```

> **需认证**。SMTP 服务器地址与端口已在注册时根据邮箱域名自动识别，此处只需填写邮箱 SMTP 授权码。

**请求参数** (Form / Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户 ID（需与当前登录用户一致） |
| email | string | 是 | 邮箱地址 |
| authCode | string | 是 | 邮箱 SMTP 授权码（非邮箱登录密码） |

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

> 授权码保存失败时（如用户 ID 不匹配）静默返回，不报错。

---

### 1.8 修改密码

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

### 2.2 用户详情

```
GET /api/user/{id}
```

> 需权限：`system:user:query`

**响应**

```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "deptId": null,
    "loginName": "zhangsan",
    "userName": "张三",
    "userType": "01",
    "email": "zhangsan@example.com",
    "phoneNumber": "13800138000",
    "sex": "0",
    "status": "0",
    "createTime": "2026-07-21T10:00:00",
    "roleIds": [1, 2],
    "remark": null
  }
}
```

> `roleIds` 为关联的角色 ID 列表；不存在时返回 `code: 500, message: "用户不存在"`

---

### 2.3 新增用户

```
POST /api/user
```

> 需权限：`system:user:add`

**请求体** (JSON)

```json
{
  "loginName": "lisi",
  "userName": "李四",
  "email": "lisi@example.com",
  "phoneNumber": "13900139000",
  "sex": "0",
  "status": "0",
  "deptId": 1,
  "password": "123456",
  "roleIds": [1, 2],
  "remark": "备注"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| loginName | string | 是 | 登录账号 |
| userName | string | 否 | 用户昵称 |
| email | string | 否 | 邮箱 |
| phoneNumber | string | 否 | 手机号 |
| sex | string | 否 | 0 男，1 女，2 未知 |
| status | string | 否 | 0 正常，1 停用（默认 0） |
| deptId | long | 否 | 部门 ID |
| password | string | 否 | 密码（不传则默认 123456） |
| roleIds | long[] | 否 | 角色 ID 列表 |
| remark | string | 否 | 备注 |

**响应** — 成功

```json
{ "code": 200, "message": "新增成功", "data": null }
```

> 可能失败原因：账号已存在

---

### 2.4 修改用户

```
PUT /api/user
```

> 需权限：`system:user:edit`

**请求体** (JSON)

```json
{
  "userId": 1,
  "loginName": "lisi_new",
  "userName": "李四(改)",
  "email": "newlisi@example.com",
  "status": "1"
}
```

> 所有字段均为可选，仅更新传入的非 null 字段。`password` 仅在显式传入时才会更新。`roleIds` 传入时会先清旧角色再绑定新角色。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户 ID |
| loginName | string | 否 | 新登录账号 |
| userName | string | 否 | 新昵称 |
| email | string | 否 | 新邮箱 |
| phoneNumber | string | 否 | 新手机号 |
| sex | string | 否 | 性别 |
| status | string | 否 | 状态 |
| deptId | long | 否 | 部门 ID |
| password | string | 否 | 新密码（不传则不改） |
| roleIds | long[] | 否 | 角色 ID 列表（全量替换） |
| remark | string | 否 | 备注 |

**响应** — 成功

```json
{ "code": 200, "message": "修改成功", "data": null }
```

> 可能失败原因：用户不存在、新账号已存在

---

### 2.5 删除用户

```
DELETE /api/user/{id}
```

> 需权限：`system:user:remove`

> 软删除，将 `delFlag` 置为 `"2"`，不会物理删除数据。

**响应**

```json
{ "code": 200, "message": "删除成功", "data": null }
```

---

### 2.6 Excel 导入用户

```
POST /api/excel/import
```

> 需权限：`system:user:import`
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | Excel 文件（.xlsx / .xls） |

**Excel 模板格式**

| 列名 | 必填 | 说明 |
|------|------|------|
| 用户名 | 是 | 登录账号 |
| 手机号 | 否 | 手机号码 |
| 邮箱 | 否 | 邮箱地址 |
| 角色 | 否 | 角色名称（需与系统中角色名一致） |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "导入完成：成功5条，跳过2条。详细信息：用户名[admin]已存在，跳过；用户[test]的角色[测试员]不存在，跳过"
}
```

> 默认密码为 `123456`，用户类型为系统用户（00）。已存在的用户名和未匹配的角色名会自动跳过。

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

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 各项字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户 ID |
| loginName | string | 登录账号 |
| roleId | long | 角色 ID |
| roleName | string | 角色名称 |

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

### 3.4 编辑

```
PUT /sys-user-role
```

> 需权限：`system:userRole:edit`

**请求体** (JSON) — `UserRole` 对象（含 `userId`、`roleId`，按 `roleId` 定位记录）

### 3.5 删除

```
DELETE /sys-user-role/{roleId}
```

> 需权限：`system:userRole:delete`

### 3.6 批量删除

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

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`，`records` 各项字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | long | 角色 ID |
| roleName | string | 角色名称 |
| menuId | long | 菜单 ID |
| menuName | string | 菜单名称 |

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

### 5.4 编辑

```
PUT /sys-role-menu
```

> 需权限：`system:roleMenu:edit`

**请求体** (JSON) — `RoleMenu` 对象（含 `roleId`、`menuId`，按 `menuId` 定位记录）

### 5.5 删除

```
DELETE /sys-role-menu/{menuId}
```

> 需权限：`system:roleMenu:delete`

### 5.6 批量删除

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

> **需认证**（根据当前用户权限过滤，无权限标识的菜单/目录所有人可见）

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

> **需认证**

### 7.3 详情

```
GET /api/mail-template/{id}
```

> **需认证**

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
| disclosureId | long | 否 | — | 关联专利交底ID，非必填 |
| to | string | 是 | — | 收件人邮箱 |
| subject | string | 是 | — | 邮件主题 |
| content | string | 是 | — | 邮件正文 |
| cc | string | 否 | — | 抄送邮箱 |
| isHtml | boolean | 否 | false | 正文是否为 HTML |
| files | file | 否 | — | 附件文件，发送成功后会记录到 `mail_send_attachment` 表 |
| disclosureAttachmentIds | long[] | 否 | — | 关联的交底附件ID列表，用于标记附件来源，逗号分隔 |

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
  "disclosureId": 1,
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
  ],
  "disclosureAttachmentIds": [1, 2]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| disclosureId | long | 否 | 关联专利交底ID，非必填 |
| to | string | 是 | 收件人，逗号/分号分隔多人 |
| cc | string | 否 | 抄送，逗号/分号分隔多人 |
| subject | string | 否 | 主题（模板优先时可为空） |
| text | string | 否 | 正文（模板优先时可为空） |
| templateCode | string | 否 | 模板编码，选用模板时传入 |
| templateData | map | 否 | 模板变量，用于 Thymeleaf 渲染 |
| attachmentUrls | string[] | 否 | 附件 URL 列表，来自上传接口返回的路径，发送成功后会记录到 `mail_send_attachment` 表 |
| disclosureAttachmentIds | long[] | 否 | 关联的交底附件ID列表，与attachmentUrls按索引一一对应，用于标记附件来源 |

---

### 9.3 邮件发送日志查询（按交底 ID）

```
GET /api/mail-send-log
```

> **需认证**

**请求参数** (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| disclosureId | long | 否 | 专利交底 ID，不传则返回全部发送记录 |

**响应** — `data` 为对象数组，每个对象包含 `mailSendLog` 和 `attachmentList`：

`mailSendLog` 字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| disclosureId | long | 关联交底 ID |
| fromEmail | string | 发件人邮箱 |
| toEmails | string | 收件人 |
| ccEmails | string | 抄送 |
| subject | string | 邮件主题 |
| content | string | 邮件正文 |
| sendStatus | int | 0 PENDING / 1 SUCCESS / 2 FAILED |
| errorMessage | string | 失败原因 |
| senderUserId | long | 发送人用户 ID |
| senderName | string | 发送人姓名 |
| businessType | string | 业务类型（如 APPLICATION_PACKAGE） |
| businessRef | string | 业务关联标识 |
| businessAction | string | 业务动作（SEND/REJECT/APPROVE/UNLOCK/SUBMIT） |
| sentAt | datetime | 实际发送时间 |
| createTime | datetime | 创建时间 |

`attachmentList` 为 `MailSendAttachment[]`，各字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| mailSendLogId | long | 关联发送记录 ID |
| disclosureAttachmentId | long | 来源交底附件 ID，可空 |
| fileName | string | 文件名 |
| filePath | string | 磁盘路径 |
| fileUrl | string | 访问 URL |
| fileSize | long | 文件大小（字节） |

---

### 9.4 邮件发送日志分页列表

```
GET /api/mail-send-log/list
```

> **需认证**

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| userId | long | 是 | — | 用户 ID |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**响应** — 分页格式 `{ records, total, pageNum, pageSize }`

---

### 9.5 邮件发送日志详情

```
GET /api/mail-send-log/{id}
```

> **需认证**

---

### 9.6 删除邮件发送日志

```
DELETE /api/mail-send-log/{id}
```

> **需认证**

**响应**
```json
{ "code": 200, "message": "删除成功", "data": null }
```

---

### 9.7 批量删除邮件发送日志

```
DELETE /api/mail-send-log/batch
```

> **需认证**

**请求体** (JSON) — `[1, 2, 3]`

---

### 9.8 重发邮件

```
POST /api/mail-send-log/resend/{id}
```

> **需认证**

根据日志 ID 重新发送失败的邮件。

**响应**
```json
{ "code": 200, "message": "重发成功", "data": null }
```

---

### 9.9 邮件接收日志 `/api/mail-receive-log`

> 对应数据表：`mail_receive_log`

#### 9.9.1 分页列表

```
GET /api/mail-receive-log/list
```

> **需认证**

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| userId | long | 是 | — | 用户 ID |
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**MailReceiveLog 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| fromEmail | string | 发件人邮箱 |
| toEmails | string | 收件人 |
| subject | string | 主题 |
| content | string | 正文 |
| senderUserId | long | 收件人用户 ID |
| senderName | string | 收件人姓名 |
| receivedAt | datetime | 接收时间 |
| createTime | datetime | 创建时间 |

#### 9.9.2 详情

```
GET /api/mail-receive-log/{id}
```

> **需认证**

#### 9.9.3 删除

```
DELETE /api/mail-receive-log/{id}
```

> **需认证**

#### 9.9.4 批量删除

```
DELETE /api/mail-receive-log/batch
```

> **需认证**

**请求体** (JSON) — `[1, 2, 3]`

---

## 十、T表 — 专利交底 `/api/ttable`

> 对应数据表：`patent_disclosure`

### 10.1 分页列表

```
GET /api/ttable/list
```

> 需权限：`patent:disclosure:list`
>
> 当登录用户角色包含 `projectInitiator` 且不包含 `admin` 时，只返回 `entryUserId` 为当前用户的数据；`search`、`all`、`by-sponsor` 遵循同一数据范围。

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
    "attachmentsByDisclosureId": {
      "1": [
        {
          "id": 101,
          "disclosureId": 1,
          "bizType": "DISCLOSURE_DOC",
          "fileName": "技术交底书.docx",
          "fileUrl": "/files/uuid.docx?name=..."
        }
      ]
    },
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

`attachmentsByDisclosureId` 只包含当前页记录的未删除附件，以交底 ID 分组；`bizType` 为 `DISCLOSURE_DOC` 时表示交底书，为 `DISCLOSURE_OTHER` 时表示其他文件。列表页可直接使用该字段展示附件，无需逐行请求附件接口。高级搜索分页响应使用相同结构。

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

### 10.3.1 主办人候选列表

```
GET /api/ttable/sponsor-options
```

> 需权限：`patent:disclosure:add` 或 `patent:disclosure:edit`

只返回账号正常、未删除并拥有 `organizer` 角色的用户。响应仅包含选择主办人所需的最小字段：

```json
{
  "code": 200,
  "data": [
    { "userId": 12, "userName": "张三", "loginName": "zhangsan" }
  ]
}
```

新增交底时必须提交选中项的 `sponsorUserId`；后端会重新校验用户状态和角色，并以系统用户姓名覆盖客户端提交的 `sponsor`，防止伪造归属。

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
    "invoices": [ { "id": 1, "invoiceType": "普票", "invoiceAmount": 500.00, "invoiceStatus": "ISSUED" } ]
  }
}
```

### 10.6 新增

```
POST /api/ttable/add
```

> 需权限：`patent:disclosure:add`
> Content-Type: `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| request | application/json | 是 | `PatentDisclosureDTO` JSON |
| disclosureDocument | file | 是 | 必须且只能一份 `.doc`/`.docx` Word 交底书 |
| otherAttachments | file[] | 否 | 其他附件，可上传一份或多份 |
| sourceId | long | 否 | 复制的历史交底 ID，仅用于记录 `copyFromId` |

`request` 仅允许提交 `PatentDisclosureDTO` 中定义的业务字段；其中的 `id` 会在新增时忽略，录入人使用当前认证用户。

系统将主记录、附件元数据、缴费记录和开票记录放在同一数据库事务中写入。附件文件上传失败或数据库事务回滚时，会清理本次已上传的文件，避免孤儿文件。缴费和开票记录的初始状态均为 `PENDING`、来源均为 `DISCLOSURE_SYNC`。

兼容地址 `POST /api/ttable/with-attachments` 与本接口行为一致。

### 10.7 修改

```
PUT /api/ttable
```

> 需权限：`patent:disclosure:edit`

> `projectInitiator`（立项专员）拥有该权限，但后端仍按 `entryUserId` 校验数据范围，只允许编辑本人录入的交底；管理员可编辑全部可见交底。
> `organizer`（主办人）只能编辑分配给自己的交底，且请求中的 `sponsorUserId` 和 `sponsor` 会被后端忽略，不能借此将交底改派给自己或其他人。

**请求体** (JSON) — `PatentDisclosureDTO` 对象，`id`、`disclosureName`、`patentType` 必填。
请求体中的服务端维护字段会被忽略。

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

> 需权限：`patent:disclosure:copy` 或 `patent:disclosure:add`

**请求体** (JSON)

```json
{ "sourceId": 1 }
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceId | long | 是 | 源交底ID |

**响应** — `data` 为用于预填录入表单的 `PatentDisclosureDTO`，不会直接创建新记录。用户补充一份 Word 交底书及可选的其他附件后，再调用新增接口；新增接口的 `sourceId` 会写入主记录的 `copyFromId`。

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
  "remark": "已定稿，进入下一流程"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| toStatus | string | 是 | 新状态 |
| remark | string | 否 | 备注/原因 |

> 操作人只能从当前 JWT 登录上下文产生，客户端不能指定。系统自动记录旧状态 → 新状态的变更日志。
> `定稿待报` 和 `已申报` 是申请包工作流专属状态，调用本接口设置会被拒绝。

### 10.17 上传附件

```
POST /api/ttable/{id}/attachments
```

> 需权限：`patent:disclosure:attachment:upload` 或 `patent:disclosure:add`
> Content-Type: `multipart/form-data`

**请求参数** (FormData)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| file | file | 是 | — | 附件文件 |
| bizType | string | 否 | DISCLOSURE_OTHER | DISCLOSURE_DOC 交底书 / DISCLOSURE_OTHER 其他 |

> `DISCLOSURE_DOC` 只能是 `.doc`/`.docx`，且每条交底只能保留一份；上传人信息取自当前认证用户。

**响应** — `data` 为新创建的 `DisclosureAttachment` 对象，包含文件访问 URL。

### 10.17.1 更换交底书

```
PUT /api/ttable/{id}/attachments/disclosure-document
```

> 需权限：`patent:disclosure:attachment:upload` 或 `patent:disclosure:add`
> Content-Type: `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 新的 `.doc`/`.docx` Word 交底书 |

新交底书文件及元数据保存成功后，旧交底书元数据才会被逻辑删除；数据库失败时会回滚元数据并清理新上传文件。

### 10.18 删除附件（逻辑删除）

```
DELETE /api/ttable/attachments/{attachmentId}
```

> 需权限：`patent:disclosure:delete`、`patent:disclosure:attachment:upload` 或 `patent:disclosure:add` 中任一权限

> 将 `deleted` 字段置为 1，不会物理删除文件。

### 10.19 申请包接口迁移说明

旧的 `GET/POST /api/ttable/{id}/packages` 已移除。组包、版本查询与审核统一使用第十三章的申请包工作流接口。

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

## 十二、代理人/申请人接口

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
GET /api/applicant/list
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
GET /api/applicant/all
```

> 需权限：`patent:applicant:list`

### 12.10 申请人详情

```
GET /api/applicant/{id}
```

> 需权限：`patent:applicant:query`

### 12.11 申请人新增

```
POST /api/applicant
```

> 需权限：`patent:applicant:add`

**请求体** (JSON) — `Applicant` 对象（`name` 必填）

### 12.12 申请人修改

```
PUT /api/applicant
```

> 需权限：`patent:applicant:edit`

**请求体** (JSON) — `Applicant` 对象，`id` 必填

### 12.13 申请人删除

```
DELETE /api/applicant/{id}
```

> 需权限：`patent:applicant:delete`

### 12.14 申请人批量删除

```
DELETE /api/applicant/batch
```

> 需权限：`patent:applicant:delete`

**请求体** (JSON) — `[1, 2, 3]`

---

## 十三、申请包接口 `/api/application-package`

> 对应 `application_package_batch` 批次主表及文件版本、审核问题、操作日志表。
> 对外只使用随机 `packageToken` / `fileToken`，不返回申请包或文件的数据库自增 ID、磁盘路径和永久文件 URL。

状态机：`DRAFT → PENDING_RECEIVE → REVIEWING → APPROVED → SUBMITTED`；退回分支为 `REVIEWING → REJECTED → PENDING_RECEIVE`，管理员可将 `APPROVED` 解锁回 `REVIEWING`。

### 13.1 分页列表

```
GET /api/application-package/batches
```

> 需权限：`patent:applicationPackage:list`

**请求参数** (Query)

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| status | string | 否 | — | 申请包状态（精确） |
| internalNo | string | 否 | — | 内部编号（模糊） |
| disclosureName | string | 否 | — | 交底名称（模糊） |
| sponsorName | string | 否 | — | 主办人（模糊） |

数据库分页并自动应用数据范围：管理员查看全部；主办人只看自己的批次；流程专员只看分配给自己的非草稿批次。

### 13.2 查询详情与按交底查询

```text
GET /api/application-package/batches/{packageToken}
GET /api/application-package/batches/by-disclosure/{disclosureId}
```

> 需权限：`patent:applicationPackage:query`

详情包含当前文件、历史文件版本、各轮审核问题和完整操作日志。无效令牌、不存在和无数据权限统一提示“申请包不存在或无权访问”。

### 13.3 创建草稿

```
POST /api/application-package/drafts
```

> 需权限：`patent:applicationPackage:compose`

```json
{ "disclosureId": 1 }
```

仅主办人本人或管理员可为“定稿”交底创建；同一交底重复调用会返回已有批次。

### 13.4 上传、替换或移除文件

```text
PUT /api/application-package/batches/{packageToken}/files/{documentCode}
DELETE /api/application-package/batches/{packageToken}/files/{documentCode}
```

> 需权限：`patent:applicationPackage:compose`；Content-Type: `multipart/form-data`

`documentCode` 为 `XML`、`REQUEST`、`DESCRIPTION`、`CLAIMS`、`ABSTRACT`、`ABSTRACT_DRAWING` 之一，上传表单字段为 `file`。只允许在 `DRAFT` / `REJECTED` 修改。移除只清空当前槽位，文件版本仍保留在历史记录中；再次上传继续递增版本号。单文件上限 10 MB。

### 13.5 发送与接收

```text
POST /api/application-package/batches/{packageToken}/send
POST /api/application-package/batches/{packageToken}/receive
GET  /api/application-package/process-operators
```

首次发送请求体为 `{ "processUserId": 123 }`（这里是流程专员的用户 ID），需 `send` 权限；重新发送仍沿用原流程专员。接收需 `receive` 权限，且必须是该批次流程专员。流程专员候选接口返回启用且绑定 `role_id=7` 的用户。

### 13.6 退回与审核通过

```text
POST /api/application-package/batches/{packageToken}/reject
POST /api/application-package/batches/{packageToken}/approve
```

> 需权限：`patent:applicationPackage:review`

退回请求体示例：

```json
{
  "reason": "整体格式不合格",
  "issues": [
    { "documentCode": "CLAIMS", "issueText": "权利要求编号不连续" }
  ]
}
```

必须先接收进入 `REVIEWING` 才能退回或通过；退回总原因必填。整体原因和具体文件问题均按原始换行保存到审核问题历史。审核通过后文件锁定，交底状态变为“定稿待报”。

### 13.7 管理解锁

```text
POST /api/application-package/batches/{packageToken}/unlock
```

> 需权限：`patent:applicationPackage:unlock`，且当前用户必须具有管理员角色。

请求体为 `{ "reason": "误操作说明" }`。只允许解锁 `APPROVED`，回到 `REVIEWING` 并恢复交底为“定稿”；`SUBMITTED` 不可解锁。

### 13.8 提交国知局

```text
POST /api/application-package/batches/{packageToken}/submit-cnipa
```

> 需权限：`patent:applicationPackage:submit`；Content-Type: `multipart/form-data`

表单字段：`submissionNo`、ISO-8601 格式 `submittedAt`、XML 文件 `receipt`，三者必填。成功后状态为 `SUBMITTED`，交底变为“已申报”，并幂等生成一条 `patent_new_application`。

### 13.9 文件下载票据

```text
POST /api/application-package/files/{fileToken}/download-ticket
GET  /api/application-package/download/{ticket}
```

第一步校验角色、数据归属和文件权限，在 Redis 生成有效期 5 分钟且与当前用户绑定的一次性票据；第二步消费票据下载。票据被原子读取并删除，不能重复使用。

### 13.10 文件与邮件规则

- XML 禁止 DTD/外部实体；`.docx` 校验 ZIP 与 `word/document.xml`；`.doc` 校验 OLE 文件头。
- 业务操作提交事务后发送邮件；邮件失败不回滚状态，操作日志显示失败原因。
- 国知局回执使用 `fileRole=CNIPA_RECEIPT`，不计入六个组包文件。

### 13.11 旧版 Word 在线预览

```text
POST /api/file-preview/legacy-word
```

使用 `multipart/form-data` 的 `file` 字段上传真实 `.doc`，最大 10 MB。成功时返回临时 `application/pdf`；转换结果只用于当前页面，不替换原文件，也不写入数据库。接口需要登录，转换任务限制并发和超时。

Spring Boot 校验文件格式后，通过内网调用 Gotenberg 的 `/forms/libreoffice/convert`。当前默认 `GOTENBERG_ENABLED=false`，未安装 Docker 时 `.doc` 在线预览关闭，但不影响文件上传、下载和其他业务。Gotenberg 不得暴露到公网；需要开启时使用仓库根目录的 `compose.gotenberg.yml`，默认只绑定 `127.0.0.1:3000`：

```text
docker compose -f compose.gotenberg.yml up -d
GOTENBERG_ENABLED=true
GOTENBERG_BASE_URL=http://127.0.0.1:3000
GOTENBERG_READ_TIMEOUT_SECONDS=70
GOTENBERG_MAX_CONCURRENT=2
```

如果 Spring Boot 与 Gotenberg 位于同一 Docker Compose 网络，使用 `GOTENBERG_BASE_URL=http://gotenberg:3000`，无需将 Gotenberg 映射到宿主机公网端口。`.docx` 仍由前端 `@vue-office/docx` 渲染；申请包当前文件、历史版本和交底附件共同复用旧版 `.doc` 转 PDF 能力。

---

## 十四、通知消息接口 `/api/notification`

> 对应数据表：`notification_message`（提醒消息表）。消息由后端定时任务扫描案件时限自动生成并写入数据库，通过 WebSocket 实时推送给在线用户。

### 14.1 标记单条消息已读

```
POST /api/notification/read/{msgId}
```

> **需认证**

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| msgId | long | 消息 ID |

只允许标记本人接收的消息；重复标记已读无副作用。

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

### 14.2 全部标记已读

```
POST /api/notification/readAll
```

> **需认证**

将当前用户所有未读消息一次性标记为已读。

**响应**

```json
{ "code": 200, "message": "success", "data": null }
```

**NotificationMessage 实体字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| userId | long | 消息接收用户 ID |
| caseId | long | 关联案件 ID |
| deadlineId | long | 关联案件期限 ID |
| title | string | 消息标题 |
| content | string | 消息正文详情 |
| link | string | 消息跳转路径（前端路由） |
| isRead | int | 0 未读 / 1 已读 |
| isPushed | int | 0 未推送 / 1 已推送 |
| plannedSendTime | datetime | 计划推送时间（规则计算得出） |
| actualSendTime | datetime | 实际推送时间 |
| isEstimateCalc | int | 0 精确计算 / 1 使用默认天数估算 |
| createTime | datetime | 创建时间 |
| readTime | datetime | 阅读时间 |

---

## 十五、WebSocket 实时推送 `/ws`

> 前端通过 WebSocket 连接接收实时通知，无需轮询 HTTP 接口。

### 连接信息

| 项目 | 值 |
|------|-----|
| 端点 | `ws://host:5050/ws` |
| 认证 | 连接时通过 `token` 查询参数携带 JWT，由 `JwtWsInterceptor` 校验 |
| 子协议 | 无 |

### 连接示例

```
ws://localhost:5050/ws?token=eyJhbGciOiJIUzI1NiJ9...
```

### 下行消息格式

服务端向客户端推送的 JSON：

```json
{
  "type": "NOTIFICATION",
  "message": {
    "id": 1,
    "title": "案件时限提醒",
    "content": "案件 [P2025101] 一种新型散热装置 的申请日止期将于 2026-08-15 到期，请及时处理",
    "link": "/patent/detail/1",
    "createTime": "2026-08-01T09:00:00"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| type | string | 消息类型，目前为 `NOTIFICATION` |
| message | object | NotificationMessage 核心字段（id / title / content / link / createTime） |

### 在线状态

服务端通过 `WsOnlineEvent` 管理在线用户集合。用户 WebSocket 连接建立后立即推送该用户未读消息（`isPushed=0`），推送成功后更新 `isPushed=1` 和 `actualSendTime`。

---

## 附录 A：权限标识汇总

### 系统管理
```
system:user:list               — 用户列表
system:user:query              — 用户详情
system:user:add                — 新增用户
system:user:edit               — 修改用户
system:user:remove             — 删除用户
system:user:import             — Excel 导入用户
system:userRole:list|add|edit|delete   — 用户角色关联
system:role:list|query|add|edit|delete — 角色管理
system:menu:list|query|add|edit|delete — 菜单管理
system:roleMenu:list|add|edit|delete    — 角色菜单关联
system:mailTemplate:list|query|add|edit|delete — 邮件模板管理
```

### T表（专利交底）
```
patent:disclosure:list    — 列表/搜索
patent:disclosure:query   — 详情/附件/日志/费用/开票
patent:disclosure:add     — 新增
patent:disclosure:copy    — 复制历史交底
patent:disclosure:attachment:upload — 上传交底附件
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
patent:applicationPackage:list|query          — 列表与详情
patent:applicationPackage:compose|send        — 主办人组包与发送
patent:applicationPackage:receive|review      — 流程专员接收与审核
patent:applicationPackage:submit              — 流程专员登记提交国知局
patent:applicationPackage:unlock              — 管理员纠错解锁
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
