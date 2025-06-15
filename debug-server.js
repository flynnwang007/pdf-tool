const jwt = require('jsonwebtoken');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

// 使用你的真实MemFireDB JWT密钥
const JWT_SECRET = '0d37e31b-3452-4949-8e19-04bc619c78c9';
const SERVER_URL = 'http://www.aibase123.cn';

// 创建测试JWT token
function createTestToken() {
    const payload = {
        sub: '12345678-1234-1234-1234-123456789012',
        aud: 'authenticated',
        role: 'authenticated',
        iss: 'memfiredb',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600 // 1小时后过期
    };
    return jwt.sign(payload, JWT_SECRET);
}

// 创建测试文件
function createTestFile() {
    const content = 'This is a test PDF file content.\nTest upload functionality.';
    fs.writeFileSync('test-file.txt', content);
    return 'test-file.txt';
}

async function testServerHealth() {
    try {
        console.log('=== 测试云服务器健康状态 ===');
        const response = await axios.get(`${SERVER_URL}/actuator/health`);
        console.log('健康检查成功:', response.data);
    } catch (error) {
        console.error('健康检查失败:', error.message);
    }
}

async function testJWTAuth() {
    try {
        console.log('\n=== 测试JWT认证 ===');
        const token = createTestToken();
        console.log('Token创建成功');
        
        // 测试GET请求（应该返回405或403）
        const response = await axios.get(`${SERVER_URL}/api/files/upload`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('JWT认证测试成功');
    } catch (error) {
        console.log('JWT认证响应 (预期的错误):', error.response?.status, error.response?.data?.message);
    }
}

async function testUploadToServer() {
    try {
        console.log('\n=== 测试云服务器文件上传 ===');
        const token = createTestToken();
        const testFile = createTestFile();
        
        const form = new FormData();
        form.append('file', fs.createReadStream(testFile));

        console.log('开始上传到云服务器...');
        const response = await axios.post(`${SERVER_URL}/api/files/upload`, form, {
            headers: {
                ...form.getHeaders(),
                'Authorization': `Bearer ${token}`
            },
            timeout: 30000 // 30秒超时
        });

        console.log('云服务器上传成功!');
        console.log('响应状态:', response.status);
        console.log('响应数据:', response.data);

    } catch (error) {
        console.error('\n云服务器上传失败:');
        console.error('状态:', error.response?.status);
        console.error('错误信息:', error.response?.data);
        console.error('完整错误:', error.message);
        
        if (error.code === 'ECONNABORTED') {
            console.error('可能是网络超时问题');
        }
    } finally {
        // 清理测试文件
        if (fs.existsSync('test-file.txt')) {
            fs.unlinkSync('test-file.txt');
        }
    }
}

async function runAllTests() {
    await testServerHealth();
    await testJWTAuth();
    await testUploadToServer();
}

runAllTests(); 