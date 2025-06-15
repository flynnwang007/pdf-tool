const jwt = require('jsonwebtoken');
const axios = require('axios');

// 使用你的真实MemFireDB JWT密钥
const JWT_SECRET = '0d37e31b-3452-4949-8e19-04bc619c78c9';

function createTestToken(customPayload = {}) {
    const payload = {
        sub: '12345678-1234-1234-1234-123456789012',
        aud: 'authenticated',
        role: 'authenticated',
        iss: 'memfiredb',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600,
        ...customPayload
    };
    return jwt.sign(payload, JWT_SECRET);
}

async function testJWTVariations() {
    const servers = [
        { name: '本地', url: 'http://localhost:8080' },
        { name: '云服务器', url: 'https://www.aibase123.cn' }
    ];

    const tokenVariations = [
        { 
            name: '标准Token', 
            token: createTestToken() 
        },
        { 
            name: 'SupabaseToken', 
            token: createTestToken({ 
                iss: 'https://d11558og91hm5619qfbg.baseapi.memfiredb.com/auth/v1',
                aud: 'authenticated'
            }) 
        },
        { 
            name: '简化Token', 
            token: createTestToken({ 
                role: 'user'
            }) 
        }
    ];

    for (const server of servers) {
        console.log(`\n=== 测试服务器: ${server.name} (${server.url}) ===`);
        
        for (const variation of tokenVariations) {
            try {
                console.log(`\n测试: ${variation.name}`);
                console.log(`Token: ${variation.token.substring(0, 50)}...`);
                
                // 验证token解析
                const decoded = jwt.verify(variation.token, JWT_SECRET);
                console.log('Token解析成功:', JSON.stringify(decoded, null, 2));
                
                // 测试API调用
                const response = await axios.get(`${server.url}/api/files/upload`, {
                    headers: {
                        'Authorization': `Bearer ${variation.token}`
                    },
                    timeout: 5000
                });
                
                console.log('API调用成功 (意外!):', response.status);
                
            } catch (jwtError) {
                if (jwtError.name === 'JsonWebTokenError') {
                    console.error('JWT验证失败:', jwtError.message);
                    continue;
                }
                
                // 这是HTTP错误，预期的
                if (jwtError.response) {
                    const status = jwtError.response.status;
                    const message = jwtError.response.data?.message || jwtError.response.data?.error;
                    
                    if (status === 403) {
                        console.log('✓ JWT认证正确，但访问被拒绝 (预期行为)');
                    } else if (status === 405) {
                        console.log('✓ JWT认证正确，方法不允许 (预期行为)');
                    } else {
                        console.log(`✗ 意外状态码: ${status}, 消息: ${message}`);
                    }
                } else {
                    console.error('✗ 连接失败:', jwtError.message);
                }
            }
        }
    }
}

async function testActualUpload() {
    console.log('\n=== 比较实际上传行为 ===');
    
    const token = createTestToken();
    const servers = [
        { name: '本地', url: 'http://localhost:8080' },
        { name: '云服务器', url: 'https://www.aibase123.cn' }
    ];

    for (const server of servers) {
        try {
            console.log(`\n测试 ${server.name} 上传...`);
            
            const FormData = require('form-data');
            const fs = require('fs');
            
            // 创建测试文件
            fs.writeFileSync('test-upload.txt', 'Test content for upload');
            
            const form = new FormData();
            form.append('file', fs.createReadStream('test-upload.txt'));

            const response = await axios.post(`${server.url}/api/files/upload`, form, {
                headers: {
                    ...form.getHeaders(),
                    'Authorization': `Bearer ${token}`
                },
                timeout: 10000
            });

            console.log(`✓ ${server.name} 上传成功:`, response.data);
            
        } catch (error) {
            console.log(`✗ ${server.name} 上传失败:`);
            console.log(`  状态: ${error.response?.status}`);
            console.log(`  错误: ${error.response?.data?.message || error.message}`);
        }
    }
    
    // 清理文件
    try {
        require('fs').unlinkSync('test-upload.txt');
    } catch (e) {}
}

async function main() {
    console.log('JWT密钥长度:', JWT_SECRET.length, '字符');
    console.log('JWT密钥:', JWT_SECRET);
    
    await testJWTVariations();
    await testActualUpload();
}

main().catch(console.error); 