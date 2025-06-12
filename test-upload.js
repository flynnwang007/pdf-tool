const jwt = require('jsonwebtoken');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

// 使用你的真实MemFireDB JWT密钥
const JWT_SECRET = '0d37e31b-3452-4949-8e19-04bc619c78c9';

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

async function testUpload() {
    try {
        console.log('1. 创建JWT token...');
        const token = createTestToken();
        console.log('Token创建成功:', token.substring(0, 50) + '...');

        console.log('\n2. 创建测试文件...');
        const testFile = createTestFile();
        console.log('测试文件创建:', testFile);

        console.log('\n3. 测试文件上传...');
        const form = new FormData();
        form.append('file', fs.createReadStream(testFile));

        const response = await axios.post('http://localhost:8080/api/files/upload', form, {
            headers: {
                ...form.getHeaders(),
                'Authorization': `Bearer ${token}`
            }
        });

        console.log('上传成功!');
        console.log('响应状态:', response.status);
        console.log('响应数据:', response.data);

    } catch (error) {
        console.error('上传失败:');
        console.error('状态:', error.response?.status);
        console.error('错误信息:', error.response?.data);
        console.error('完整错误:', error.message);
    } finally {
        // 清理测试文件
        if (fs.existsSync('test-file.txt')) {
            fs.unlinkSync('test-file.txt');
            console.log('\n测试文件已清理');
        }
    }
}

testUpload(); 