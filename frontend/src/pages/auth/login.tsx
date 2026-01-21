export default function Login() {
  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white rounded-lg shadow p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold mb-8 text-center">Login</h1>
        <form className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input type="email" className="w-full border rounded px-3 py-2" placeholder="your@email.com" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input type="password" className="w-full border rounded px-3 py-2" placeholder="" />
          </div>
          <button type="submit" className="w-full bg-blue-600 text-white rounded py-2 hover:bg-blue-700">
            Sign In
          </button>
        </form>
        <p className="text-center mt-4 text-sm">
          Don''t have an account? <a href="/auth/register" className="text-blue-600">Register</a>
        </p>
      </div>
    </div>
  );
}