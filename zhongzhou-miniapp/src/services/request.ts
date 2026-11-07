import Taro from '@tarojs/taro'

declare const process: { env?: Record<string, string | undefined> }

/** 后端 SpringBoot 服务地址：构建时可注入 TARO_APP_API_BASE，默认本机 8080 */
const envBase = typeof process !== 'undefined' && process.env ? process.env.TARO_APP_API_BASE : ''
export const BASE_URL = envBase || 'http://localhost:8080'

export const TOKEN_KEY = 'family_token'
export const FAMILY_KEY = 'family_info'
export const ELDERS_KEY = 'family_elders'

interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, any>
  /** 是否跳过登录失效跳转（登录页使用） */
  skipAuthRedirect?: boolean
}

/** 清理本地登录信息 */
export function clearAuth() {
  Taro.removeStorageSync(TOKEN_KEY)
  Taro.removeStorageSync(FAMILY_KEY)
  Taro.removeStorageSync(ELDERS_KEY)
}

/** 统一请求封装：自动携带家属端凭证、统一处理业务错误 */
export function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = Taro.getStorageSync(TOKEN_KEY) || ''
  return new Promise<T>((resolve, reject) => {
    Taro.request({
      url: `${BASE_URL}${path}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json; charset=utf-8',
        'X-Family-Token': token
      },
      success: (res) => {
        const body = res.data as ApiResult<T>
        if (res.statusCode === 200 && body && body.code === 200) {
          resolve(body.data)
          return
        }
        if (body && body.code === 401) {
          console.warn('[FamilyApi] 登录已失效', path)
          clearAuth()
          if (!options.skipAuthRedirect) {
            Taro.redirectTo({ url: '/pages/login/index' })
          }
          reject(new Error(body.msg || '登录已过期，请重新登录'))
          return
        }
        console.error(`[FamilyApi] ${path} 请求失败`, res.statusCode, body)
        reject(new Error((body && body.msg) || '请求失败，请稍后重试'))
      },
      fail: (err) => {
        console.error(`[FamilyApi] ${path} 网络异常`, err)
        reject(new Error('网络连接失败，请确认后端服务已启动'))
      }
    })
  })
}
