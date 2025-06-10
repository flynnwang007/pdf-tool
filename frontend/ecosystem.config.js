module.exports = {
  apps: [
    {
      name: 'pdf-frontend-dev',
      script: 'npm',
      args: 'run dev',
      cwd: '/root/pdf-tool/frontend',
      env: {
        NODE_ENV: 'development',
        PORT: 3000
      },
      instances: 1,
      autorestart: true,
      watch: false,
      max_memory_restart: '1G',
      error_file: './logs/err.log',
      out_file: './logs/out.log',
      log_file: './logs/combined.log',
      time: true,
      // 开发模式的特殊配置
      ignore_watch: ["node_modules", "logs"],
      exp_backoff_restart_delay: 100
    }
  ]
} 