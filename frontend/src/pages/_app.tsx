'use client';

import React, { useEffect } from 'react';
import { useAuthStore } from '@/stores/authStore';
import '@/styles/globals.css';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const initializeAuth = useAuthStore((state) => state.initializeAuth);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  return (
    <html lang="en">
      <body>
        <div id="root">{children}</div>
      </body>
    </html>
  );
}