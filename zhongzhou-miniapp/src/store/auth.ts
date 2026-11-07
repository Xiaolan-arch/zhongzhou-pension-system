import Taro from '@tarojs/taro'
import { create } from 'zustand'
import type { ElderVO, FamilyInfo } from '../types'
import { login as loginApi, logout as logoutApi, getElders } from '../services/family'
import { clearAuth, ELDERS_KEY, FAMILY_KEY, TOKEN_KEY } from '../services/request'

interface AuthState {
  token: string
  family: FamilyInfo | null
  elders: ElderVO[]
  /** 从本地缓存恢复登录态 */
  hydrate: () => void
  /** 验证码登录（wxCode 为小程序登录 code） */
  login: (phone: string, code: string, wxCode?: string) => Promise<void>
  /** 刷新绑定老人 */
  refreshElders: () => Promise<ElderVO[]>
  /** 退出登录 */
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: '',
  family: null,
  elders: [],

  hydrate: () => {
    const token = Taro.getStorageSync(TOKEN_KEY) || ''
    const family = Taro.getStorageSync(FAMILY_KEY) || null
    const elders = Taro.getStorageSync(ELDERS_KEY) || []
    set({ token, family, elders })
  },

  login: async (phone, code, wxCode) => {
    const data = await loginApi(phone, code, wxCode)
    Taro.setStorageSync(TOKEN_KEY, data.token)
    Taro.setStorageSync(FAMILY_KEY, data.family)
    Taro.setStorageSync(ELDERS_KEY, data.elders || [])
    set({ token: data.token, family: data.family, elders: data.elders || [] })
  },

  refreshElders: async () => {
    const elders = await getElders()
    Taro.setStorageSync(ELDERS_KEY, elders)
    set({ elders })
    return elders
  },

  logout: async () => {
    try {
      if (get().token) {
        await logoutApi()
      }
    } catch (e) {
      console.warn('[Auth] 退出登录接口调用失败', e)
    }
    clearAuth()
    set({ token: '', family: null, elders: [] })
  }
}))

/** 是否已登录（本地存在 token） */
export function hasAuth(): boolean {
  return !!Taro.getStorageSync(TOKEN_KEY)
}
