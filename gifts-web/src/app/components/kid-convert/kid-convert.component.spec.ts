import {of, throwError} from 'rxjs';
import {UntypedFormBuilder} from '@angular/forms';
import {KidConvertComponent} from './kid-convert.component';

describe('KidConvertComponent', () => {
  let apiSrv: jasmine.SpyObj<any>;
  let alertSrv: jasmine.SpyObj<any>;
  let authSrv: any;
  let router: jasmine.SpyObj<any>;
  let component: KidConvertComponent;
  const invite = {name: 'Jas Nowak', username: 'jas', email: 'kid@test.com'};

  function create(doc: any = document): KidConvertComponent {
    const route: any = {params: of({token: 'tok'})};
    return new KidConvertComponent(new UntypedFormBuilder(), route, router, apiSrv, alertSrv, authSrv, doc);
  }

  function fillForm(password = 'password1', confirm = 'password1') {
    component.form.patchValue({username: 'jas', password, confirmPassword: confirm});
  }

  beforeEach(() => {
    apiSrv = jasmine.createSpyObj('ApiService', ['get', 'post']);
    alertSrv = jasmine.createSpyObj('AlertService', ['success', 'error']);
    router = jasmine.createSpyObj('Router', ['navigate']);
    authSrv = {
      currentAccount: {id: 'admin'},
      setLanguage: jasmine.createSpy('setLanguage'),
      logout: jasmine.createSpy('logout').and.returnValue(of({}))
    };
  });

  describe('invite loading', () => {
    it('prefills form from invite', () => {
      apiSrv.get.and.returnValue(of(invite));

      component = create();
      component.ngOnInit();

      expect(component.invalid).toBeFalse();
      expect(component.form.controls.username.value).toBe('jas');
    });

    it('marks invite as invalid when api rejects token', () => {
      apiSrv.get.and.returnValue(throwError({status: 404}));

      component = create();
      component.ngOnInit();

      expect(component.invalid).toBeTrue();
      expect(component.form).toBeUndefined();
    });
  });

  describe('convert', () => {
    beforeEach(() => {
      apiSrv.get.and.returnValue(of(invite));
      component = create();
      component.ngOnInit();
    });

    it('rejects mismatching passwords without calling api', () => {
      fillForm('password1', 'password2');

      component.convert();

      expect(component.form.hasError('notSame')).toBeTrue();
      expect(apiSrv.post).not.toHaveBeenCalled();
    });

    it('logs out existing session and redirects to login on success', () => {
      apiSrv.post.and.returnValue(of({result: 'converted'}));
      fillForm();

      component.convert();

      expect(apiSrv.post).toHaveBeenCalledWith(jasmine.stringMatching(/kid-convert\/tok\/local$/), jasmine.objectContaining({username: 'jas', password: 'password1'}));
      expect(authSrv.logout).toHaveBeenCalled();
      expect(authSrv.currentAccount).toBeNull();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('redirects to login even when logout fails', () => {
      apiSrv.post.and.returnValue(of({}));
      authSrv.logout.and.returnValue(throwError({status: 401}));
      fillForm();

      component.convert();

      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('marks username as taken when api reports conflict', () => {
      apiSrv.post.and.returnValue(throwError({status: 409, error: 'username'}));
      fillForm();

      component.convert();

      expect(component.form.controls.username.hasError('username')).toBeTrue();
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('shows invalid state when invite expired meanwhile', () => {
      apiSrv.post.and.returnValue(throwError({status: 404, error: 'invalid'}));
      fillForm();

      component.convert();

      expect(component.invalid).toBeTrue();
    });
  });

  describe('google conversion', () => {
    const fakeDoc = (search: string): any => ({location: {search, href: 'http://host/gifts/#/convert/tok'}});

    it('builds server side google url with invite token and return address', () => {
      apiSrv.get.and.returnValue(of(invite));
      component = create(fakeDoc(''));
      component.ngOnInit();

      expect(component.googleUrl).toBe('/gifts/oauth2/kid-convert/google?token=tok&redirect_uri='
        + encodeURIComponent('http://host/gifts/#/convert/tok'));
    });

    it('logs out existing session and redirects to login when server reports conversion', () => {
      component = create(fakeDoc('?converted=true'));
      component.ngOnInit();

      expect(apiSrv.get).not.toHaveBeenCalled();
      expect(authSrv.logout).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('detects conversion reported in hash route query, which survives dev server redirects', () => {
      const route: any = {params: of({token: 'tok'}), snapshot: {queryParams: {converted: 'true'}}};
      component = new KidConvertComponent(new UntypedFormBuilder(), route, router, apiSrv, alertSrv, authSrv, fakeDoc(''));
      component.ngOnInit();

      expect(apiSrv.get).not.toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('shows email mismatch error returned by server', () => {
      apiSrv.get.and.returnValue(of(invite));
      component = create(fakeDoc('?error=kid_convert_email'));
      component.ngOnInit();

      expect(alertSrv.error).toHaveBeenCalledWith('user.kidConvert.error.email');
    });
  });
});
