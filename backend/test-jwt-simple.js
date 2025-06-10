const jwt = require('jsonwebtoken');

// 使用与后端相同的JWT密钥
const secret = '0d37e31b-3452-4949-8e19-04bc619c78c9';

console.log('🔐 简单JWT测试');
console.log('==================================================');
console.log('🔑 JWT密钥:', secret);
console.log('🔑 密钥长度:', secret.length, '字符');

// 生成JWT
const payload = {
    sub: 'test-user-12345',
    email: 'test@example.com',
    iss: 'supabase',
    aud: 'authenticated',
    iat: Math.floor(Date.now() / 1000),
    exp: Math.floor(Date.now() / 1000) + (60 * 60), // 1 hour
    role: 'authenticated'
};

console.log('\n📋 载荷:', JSON.stringify(payload, null, 2));

const token = jwt.sign(payload, secret, { algorithm: 'HS256' });
console.log('\n✅ 生成的JWT:', token);

// 验证JWT
try {
    const decoded = jwt.verify(token, secret, { algorithms: ['HS256'] });
    console.log('\n✅ JWT验证成功!');
    console.log('📋 解码后的载荷:', JSON.stringify(decoded, null, 2));
} catch (error) {
    console.log('\n❌ JWT验证失败:', error.message);
}

// 测试服务器验证
console.log('\n🧪 测试服务器验证...');
const testPayload = JSON.stringify({ token: token });

const { exec } = require('child_process');
exec(`curl -X POST -H "Content-Type: application/json" -d '${testPayload}' http://localhost:8080/public/jwt/validate`, 
    (error, stdout, stderr) => {
        if (error) {
            console.log('❌ 服务器请求失败:', error.message);
        } else {
            console.log('📡 服务器响应:', stdout);
        }
    }); 