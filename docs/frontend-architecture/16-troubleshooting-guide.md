# 16. TROUBLESHOOTING GUIDE

### Common Issues

**Issue: Build fails with "Module not found"**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json .next
npm install
npm run build
```

**Issue: Port 3000 already in use**
```bash
# Use different port
PORT=3001 npm run dev

# Or kill existing process
lsof -i :3000
kill -9 <PID>
```

**Issue: API calls return 401 Unauthorized**
```typescript
// Check token is being sent
// 1. Verify token in authStore
console.log(useAuthStore.getState().token);

// 2. Check Authorization header in Network tab
// 3. Verify token hasn't expired
// 4. Check API CORS configuration
```

**Issue: Hydration mismatch error**
```typescript
// Use dynamic imports for client-only components
const Component = dynamic(() => import('@/components/Component'), {
  ssr: false,
  loading: () => <Skeleton />,
});
```

---
