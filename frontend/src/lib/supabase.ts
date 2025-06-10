import { createClient } from '@supabase/supabase-js'

// 生产环境必须设置环境变量，开发环境可以使用默认值
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || (
  import.meta.env.MODE === 'production' 
    ? (() => { throw new Error('生产环境必须设置 VITE_SUPABASE_URL 环境变量') })()
    : 'https://d11558og91hm5619qfbg.baseapi.memfiredb.com'
)

const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || (
  import.meta.env.MODE === 'production'
    ? (() => { throw new Error('生产环境必须设置 VITE_SUPABASE_ANON_KEY 环境变量') })()
    : 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImV4cCI6MzMyNTk3Njk5NSwiaWF0IjoxNzQ5MTc2OTk1LCJpc3MiOiJzdXBhYmFzZSJ9.fqRSc8fZxx5V8SgCWZME-eTuhc2A3bOIOE9iDympXWo'
)

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error('Missing Supabase environment variables')
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: true
  }
})

export type AuthUser = {
  id: string
  email?: string
  user_metadata?: {
    [key: string]: any
  }
} 