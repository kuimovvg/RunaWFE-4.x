// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#ifndef _ARRAY_PTR_H_UID0000005D7A8B395B
#define _ARRAY_PTR_H_UID0000005D7A8B395B

#include <memory>

namespace std
{
//------------------------------------------------------

	template<class _Ty>
	struct array_ptr_ref
	{	// proxy reference for auto_ptr copying
		explicit array_ptr_ref(_Ty *_Right)
			: _Ref(_Right)
		{	// construct from generic pointer to auto_ptr ptr
		}

		_Ty *_Ref;	// generic pointer to auto_ptr ptr
	};

//------------------------------------------------------

	template<class _Ty>
	class array_ptr
	{	// wrap an object pointer to ensure destruction
	public:
		typedef array_ptr<_Ty> _Myt;
		typedef _Ty element_type;

		explicit array_ptr(_Ty *_Ptr = 0) _THROW0()
			: _Myptr(_Ptr)
		{	// construct from object pointer
		}

		array_ptr(_Myt& _Right) _THROW0()
			: _Myptr(_Right.release())
		{	// construct by assuming pointer from _Right array_ptr
		}

		array_ptr(array_ptr_ref<_Ty> _Right) _THROW0()
		{	// construct by assuming pointer from _Right array_ptr_ref
			_Ty *_Ptr = _Right._Ref;
			_Right._Ref = 0;	// release old
			_Myptr = _Ptr;	// reset this
		}

		template<class _Other>
		operator array_ptr<_Other>() _THROW0()
		{	// convert to compatible array_ptr
			return (array_ptr<_Other>(*this));
		}

		template<class _Other>
		operator array_ptr_ref<_Other>() _THROW0()
		{	// convert to compatible array_ptr_ref
			_Other *_Cvtptr = _Myptr;	// test implicit conversion
			array_ptr_ref<_Other> _Ans(_Cvtptr);
			_Myptr = 0;	// pass ownership to array_ptr_ref
			return (_Ans);
		}

		template<class _Other>
		_Myt& operator=(array_ptr<_Other>& _Right) _THROW0()
		{	// assign compatible _Right (assume pointer)
			reset(_Right.release());
			return (*this);
		}

		template<class _Other>
		array_ptr(array_ptr<_Other>& _Right) _THROW0()
			: _Myptr(_Right.release())
		{	// construct by assuming pointer from _Right
		}

		_Myt& operator=(_Myt& _Right) _THROW0()
		{	// assign compatible _Right (assume pointer)
			reset(_Right.release());
			return (*this);
		}

		_Myt& operator=(array_ptr_ref<_Ty> _Right) _THROW0()
		{	// assign compatible _Right._Ref (assume pointer)
			_Ty *_Ptr = _Right._Ref;
			_Right._Ref = 0;	// release old
			reset(_Ptr);	// set new
			return (*this);
		}

		~array_ptr()
		{	// destroy the object
			delete[] _Myptr;
		}

		_Ty& operator*() const _THROW0()
		{	// return designated value
	#if _ITERATOR_DEBUG_LEVEL == 2
			if (_Myptr == 0)
				_DEBUG_ERROR("array_ptr not dereferencable");
	#endif /* _ITERATOR_DEBUG_LEVEL == 2 */

			return (*get());
		}

		_Ty *operator->() const _THROW0()
		{	// return pointer to class object
	#if _ITERATOR_DEBUG_LEVEL == 2
			if (_Myptr == 0)
				_DEBUG_ERROR("array_ptr not dereferencable");
	#endif /* _ITERATOR_DEBUG_LEVEL == 2 */

			return (get());
		}

		_Ty *get() const _THROW0()
		{	// return wrapped pointer
			return (_Myptr);
		}

		_Ty *release() _THROW0()
		{	// return wrapped pointer and give up ownership
			_Ty *_Tmp = _Myptr;
			_Myptr = 0;
			return (_Tmp);
		}

		void reset(_Ty *_Ptr = 0)
		{	// destroy designated object and store new pointer
			if (_Ptr != _Myptr)
				delete[] _Myptr;
			_Myptr = _Ptr;
		}

	private:
		_Ty *_Myptr;	// the wrapped object pointer
	};

//------------------------------------------------------
}
//------------------------------------------------------
#endif //ifndef _ARRAY_PTR_H_UID0000005D7A8B395B
