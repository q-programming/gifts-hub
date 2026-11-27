import {of, throwError} from 'rxjs';
import {KidDialogComponent} from './kid-dialog.component';

describe('KidDialogComponent', () => {
  let apiSrv: jasmine.SpyObj<any>;
  let alertSrv: jasmine.SpyObj<any>;
  let dialogRef: jasmine.SpyObj<any>;

  function create(account: any): KidDialogComponent {
    return new KidDialogComponent(dialogRef, {account}, document, apiSrv, alertSrv);
  }

  beforeEach(() => {
    apiSrv = jasmine.createSpyObj('ApiService', ['post']);
    alertSrv = jasmine.createSpyObj('AlertService', ['success', 'error']);
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  describe('commitKid', () => {
    it('closes dialog with kid without avatar when none was uploaded', () => {
      const component = create({name: 'Jas', surname: 'Nowak', username: 'jas'});

      component.commitKid(true);

      expect(dialogRef.close).toHaveBeenCalledWith(jasmine.objectContaining({name: 'Jas', avatar: undefined}));
    });

    it('strips data url prefix from uploaded avatar', () => {
      const component = create({name: 'Jas', surname: 'Nowak', username: 'jas'});
      component.avatarData = 'data:image/png;base64,AAAA';

      component.commitKid(true);

      expect(dialogRef.close).toHaveBeenCalledWith(jasmine.objectContaining({avatar: 'AAAA'}));
    });
  });

  describe('convertKid', () => {
    let component: KidDialogComponent;

    beforeEach(() => {
      component = create({id: 'kid1', name: 'Jas', surname: 'Nowak', username: 'jas'});
    });

    it('does not call api for invalid email', () => {
      component.convertEmail.setValue('not-an-email');

      component.convertKid();

      expect(apiSrv.post).not.toHaveBeenCalled();
    });

    it('sends invite and closes dialog', () => {
      apiSrv.post.and.returnValue(of({}));
      component.convertEmail.setValue('kid@test.com');

      component.convertKid();

      expect(apiSrv.post).toHaveBeenCalledWith(jasmine.stringMatching(/kid-convert$/), {kidId: 'kid1', email: 'kid@test.com'});
      expect(alertSrv.success).toHaveBeenCalled();
      expect(dialogRef.close).toHaveBeenCalled();
    });

    it('marks email as duplicated on conflict', () => {
      apiSrv.post.and.returnValue(throwError({status: 409}));
      component.convertEmail.setValue('kid@test.com');

      component.convertKid();

      expect(component.convertEmail.hasError('duplicated')).toBeTrue();
      expect(dialogRef.close).not.toHaveBeenCalled();
    });

    it('shows error when mail could not be sent', () => {
      apiSrv.post.and.returnValue(throwError({status: 500}));
      component.convertEmail.setValue('kid@test.com');

      component.convertKid();

      expect(alertSrv.error).toHaveBeenCalledWith('user.kidConvert.invite.error');
    });
  });
});
