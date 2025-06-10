const crypto = require('crypto');

// 从.env文件读取JWT密钥
const fs = require('fs');
const path = require('path');

function loadEnvFile() {
    const envPath = path.join(__dirname, '..', '.env');
    if (fs.existsSync(envPath)) {
        const envContent = fs.readFileSync(envPath, 'utf8');
        const lines = envContent.split('\n');
        const env = {};
        
        lines.forEach(line => {
            if (line.trim() && !line.startsWith('#')) {
                const [key, ...valueParts] = line.split('=');
                if (key && valueParts.length > 0) {
                    env[key.trim()] = valueParts.join('=').trim();
                }
            }
        });
        
        return env;
    }
    return {};
}

function createTestJWT() {
    const env = loadEnvFile();
    const secret = env.MEMFIRE_JWT_SECRET || env.VITE_SUPABASE_ANON_KEY;
    
    if (!secret) {
        console.log('❌ 未找到JWT密钥，请检查.env文件');
        return null;
    }

    console.log('🔑 使用JWT密钥长度:', secret.length);

    // 创建JWT Header
    const header = {
        "alg": "HS256",
        "typ": "JWT"
    };

    // 创建JWT Payload
    const now = Math.floor(Date.now() / 1000);
    const payload = {
        "sub": "test-user-12345",           // 用户ID
        "email": "test@example.com",       // 邮箱
        "iss": "supabase",                 // 发行者
        "aud": "authenticated",            // 受众
        "iat": now,                        // 签发时间
        "exp": now + 3600,                 // 过期时间（1小时后）
        "role": "authenticated"
    };

    // 编码Header和Payload
    const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
    const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');

    // 创建签名
    const data = `${encodedHeader}.${encodedPayload}`;
    const signature = crypto
        .createHmac('sha256', secret)
        .update(data)
        .digest('base64url');

    // 组合JWT
    const jwt = `${encodedHeader}.${encodedPayload}.${signature}`;

    console.log('✅ 生成的测试JWT:');
    console.log(jwt);
    console.log('\n📋 JWT载荷信息:');
    console.log(JSON.stringify(payload, null, 2));

    return jwt;
}

async function testJWTValidation(jwt) {
    if (!jwt) return;

    console.log('\n🧪 测试JWT验证...');

    try {
        // 测试API调用
        const response = await fetch('http://localhost:8080/api/files', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${jwt}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('📡 API响应状态:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('✅ JWT验证成功！');
            console.log('📄 响应数据:', JSON.stringify(data, null, 2));
        } else {
            console.log('❌ JWT验证失败');
            const errorText = await response.text();
            console.log('错误信息:', errorText);
        }
    } catch (error) {
        console.log('❌ API调用失败:', error.message);
        console.log('请确保后端服务正在运行 (http://localhost:8080)');
    }
}

// 运行测试
console.log('🔐 JWT验证测试工具');
console.log('='.repeat(50));

const testJWT = createTestJWT();
if (testJWT) {
    // 等待一下，然后测试API
    setTimeout(() => {
        testJWTValidation(testJWT);
    }, 1000);
} 